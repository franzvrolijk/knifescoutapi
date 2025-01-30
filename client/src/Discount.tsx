import { discountColor } from "./styleHelper.ts";
import {EntryModel} from "./types.ts";

function Discount({ entry }: {entry: EntryModel}) {
  return (
    <div className="discountContainer">
      {entry.tradeVolume < 20 && (
        <span data-title="Low trade volume" className="warning">
          !
        </span>
      )}
      <p data-title="Discount relative to base price on CSFloat" style={{ color: discountColor(entry.discountPercentage) }}>
        Discount: {entry.discountPercentage.toFixed(2)}%
      </p>
    </div>
  );
}

export default Discount;
