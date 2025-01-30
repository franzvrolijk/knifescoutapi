import { useEffect, useState } from "react";
import "./App.css";
import { EntryModel } from "./types.ts";
import Entry from "./Entry.tsx";
import { getBaseUrl } from "./config.ts";

function App() {
  const [entries, setEntries] = useState<EntryModel[]>([]);
  const [loading, setLoading] = useState(true);
  const [displayLowVolume, setDisplayLowVolume] = useState(false);

  useEffect(() => {
    setLoading(true);

    fetch(`${getBaseUrl()}/api/csfloat/baseprice`, { method: "GET", mode: "cors" })
      .then((res) => res.json())
      .then((data) => {
        setEntries(data);
        setLoading(false);
      });
  }, []);

  return (
    <>
      <div className="App">
        <div className="header">
          <h1>KnifeScout</h1>
          <div className="checkbox-wrapper">
            <label htmlFor="displayLowVolume">Display low volume items:</label>
            <input id="displayLowVolume" className="checkbox" type="checkbox" checked={displayLowVolume} onChange={() => setDisplayLowVolume(!displayLowVolume)}></input>
          </div>
        </div>
        {!loading && (
          <div className="entryContainer">
            {entries.map((e) => (
              <Entry entry={e} displayLowVolume={displayLowVolume} key={e.id}></Entry>
            ))}
          </div>
        )}
      </div>
    </>
  );
}

export default App;
