import { useState } from "react";
import { CheapestModel, EntryModel } from "./types";
import { getBaseUrl } from "./config";

export default function Cheapest({ entry }: { entry: EntryModel }) {
  const [cheapest, setCheapest] = useState<CheapestModel | null>(null);

  const getCheapest = async () => {
    if (cheapest) return;
    const url = `${getBaseUrl()}/api/cheapest/${entry.name}`;
    const res = await fetch(url, { method: "GET", mode: "cors" });
    const data = await res.json();
    setCheapest(data);
  };

  return (
    <div style={{ height: "41px", marginTop: "20px", marginBottom: "0px", display: "flex", justifyContent: "center", alignItems: "center" }}>
      {!cheapest && (
        <button onClick={getCheapest} className="btn">
          Find cheapest
        </button>
      )}
      {cheapest && cheapest.price < entry.price && (
        <p>
          Found for ${cheapest.price} at {cheapest.marketName}
        </p>
      )}
      {cheapest && cheapest.price >= entry.price && <p>No cheaper offer found</p>}
    </div>
  );
}
