export function getBaseUrl(): string {
  const baseUrl: string | undefined = import.meta.env.VITE_API_BASE;

  if (!baseUrl || !baseUrl.startsWith("http")) {
    throw new Error("No API base URL provided");
  }

  return baseUrl;
}
