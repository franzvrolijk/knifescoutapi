import Cheapest from "./Cheapest.tsx";
import Discount from "./Discount.js";
import { discountEntryColor } from "./styleHelper.ts";
import { EntryModel } from "./types.ts";

export default function Entry({ entry, displayLowVolume }: { entry: EntryModel; displayLowVolume: boolean }) {
  if (!displayLowVolume && entry.tradeVolume < 20) return <></>;

  return (
    <div className="entry" style={{ background: discountEntryColor(entry.discountPercentage) }}>
      <h2>{entry.name}</h2>
      <img src={entry.iconUrl} alt={entry.name} width={300}></img>
      <p>Price: ${entry.price}</p>
      <Discount entry={entry}></Discount>
      <div className="btnContainer">
        <Cheapest entry={entry}></Cheapest>
        <button onClick={() => window.open(`https://csfloat.com/item/${entry.id}`)} className="btn">
          Buy
        </button>
      </div>
    </div>
  );
}
