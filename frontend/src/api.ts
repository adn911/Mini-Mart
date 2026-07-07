export type HealthResponse = {
  status: string;
  service: string;
};

export async function getBackendHealth(): Promise<HealthResponse> {
  const response = await fetch("/api/health");

  if (!response.ok) {
    throw new Error("Unable to reach Mini-Mart backend");
  }

  return response.json() as Promise<HealthResponse>;
}
