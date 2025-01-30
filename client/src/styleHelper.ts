export function discountColor(discount: number) {
  if (discount > 10) return "#00ff4c";
  if (discount > 7) return "#29e360";
  if (discount >= 3) return "#53e67e";
  if (discount > 0) return "#ff9955";
  return "#ed4747";
}

export function discountEntryColor(discount: number) {
  if (discount > 5) return `linear-gradient(${Math.random() * 360}deg, rgba(42,190,110,1) 0%, rgba(100, 190,150,1) 100%)`;
  if (discount >= 4) return `linear-gradient(${Math.random() * 360}deg, rgba(90,120,110,1) 0%, rgba(144,160,150,1) 100%)`;
  return "linear-gradient(148deg, rgba(144,160,150,0.6) 0%, rgba(70,80,70,0.6) 100%)";
}
