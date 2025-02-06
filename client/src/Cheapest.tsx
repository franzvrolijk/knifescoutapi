import { useState } from "react";
import { CheapestModel, EntryModel } from "./types";
import { getBaseUrl } from "./config";

export default function Cheapest({ entry }: { entry: EntryModel }) {
  const [cheapest, setCheapest] = useState<CheapestModel | null>(null);
  const [secondCheapest, setSecondCheapest] = useState<EntryModel | null>(null);

  const marketCheck = async () => {
    if (cheapest) return;
    const cheapestUrl = `${getBaseUrl()}/api/cheapest/${entry.name}`;
    const res = await fetch(cheapestUrl, { method: "GET", mode: "cors" });
    const data = await res.json();
    setCheapest(data);

    const secondCheapestUrl = `${getBaseUrl()}/api/csfloat/secondcheapest/${entry.name}/${entry.defIndex}/${entry.paintIndex}`;
    const res2 = await fetch(secondCheapestUrl, { method: "GET", mode: "cors" });
    const data2 = await res2.json();
    setSecondCheapest(data2);
  };

  return (
    <div style={{ display: "flex", height: "80px", flexDirection: "column", justifyContent: "center", alignItems: "center" }}>
      {!cheapest && (
        <button onClick={marketCheck} className="btn">
          Market check
        </button>
      )}
      {cheapest && secondCheapest && (
        <div>
          <p>
            7d cheapest: {cheapest.marketName} - ${cheapest.price}
          </p>
          <p>2nd cheapest on CSGOFloat: {secondCheapest?.price}</p>
        </div>
      )}
    </div>
  );
}
