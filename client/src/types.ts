export interface EntryModel {
  name: string;
  price: number;
  basePrice: number;
  iconUrl: string;
  discountPercentage: number;
  id: string;
  tradeVolume: number;
}

export interface CheapestModel {
  price: number;
  marketName: string;
}
