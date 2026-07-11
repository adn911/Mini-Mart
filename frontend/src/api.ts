export type Category = {
  id: number;
  name: string;
};

export type Product = {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  imageUrl: string;
  status: string;
  category: Category;
};

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

export async function getCategories(): Promise<Category[]> {
  const response = await fetch("/api/categories");

  if (!response.ok) {
    throw new Error("Unable to fetch categories");
  }

  return response.json() as Promise<Category[]>;
}

export type LoginResponse = {
  token: string;
};

const TOKEN_KEY = "mini-mart-admin-token";

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

async function authFetch(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const token = getStoredToken();
  const headers = { ...init?.headers } as Record<string, string>;
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return fetch(input, { ...init, headers });
}

export async function adminLogin(username: string, password: string): Promise<LoginResponse> {
  const response = await fetch("/api/admin/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Invalid credentials");
  }

  return response.json() as Promise<LoginResponse>;
}

export async function getAdminMe(): Promise<{ username: string }> {
  const response = await authFetch("/api/admin/me");

  if (!response.ok) {
    throw new Error("Unauthorized");
  }

  return response.json() as Promise<{ username: string }>;
}

export async function getProducts(search?: string, categoryId?: number): Promise<Product[]> {
  const params = new URLSearchParams();
  if (search) params.set("search", search);
  if (categoryId) params.set("categoryId", String(categoryId));

  const query = params.toString();
  const url = query ? `/api/products?${query}` : "/api/products";

  const response = await fetch(url);

  if (!response.ok) {
    throw new Error("Unable to fetch products");
  }

  return response.json() as Promise<Product[]>;
}
