import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, test, vi } from "vitest";

import App from "./App";

const beverages = { id: 1, name: "Beverages" };
const snacks = { id: 2, name: "Snacks" };
const pantry = { id: 3, name: "Pantry" };

const mockCategories = [beverages, snacks, pantry];

const sparklingWater = {
  id: 1,
  name: "Sparkling Water",
  description: "Premium sparkling mineral water",
  price: 4.99,
  stockQuantity: 100,
  reservedQuantity: 0,
  availableQuantity: 100,
  imageUrl: "https://picsum.photos/seed/beverage1/400/400",
  status: "ACTIVE",
  category: beverages,
};

const darkChocolate = {
  id: 2,
  name: "Dark Chocolate Bar",
  description: "72% cacao dark chocolate",
  price: 4.49,
  stockQuantity: 90,
  reservedQuantity: 0,
  availableQuantity: 90,
  imageUrl: "https://picsum.photos/seed/snack2/400/400",
  status: "ACTIVE",
  category: snacks,
};

const allProducts = [sparklingWater, darkChocolate];

function jsonResponse(data: unknown) {
  return new Response(JSON.stringify(data), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

function mockFetch() {
  vi.stubGlobal("fetch", vi.fn((input: RequestInfo | URL) => {
    const url = typeof input === "string" ? input : input.toString();
    if (url.includes("/api/categories")) {
      return Promise.resolve(jsonResponse(mockCategories));
    }
    if (url.includes("categoryId=1")) {
      return Promise.resolve(jsonResponse([sparklingWater]));
    }
    if (url.includes("categoryId=2")) {
      return Promise.resolve(jsonResponse([darkChocolate]));
    }
    if (url.includes("/api/admin/login")) {
      return Promise.resolve(jsonResponse({ token: "test-token" }));
    }
    if (url.includes("/api/admin/me")) {
      return Promise.resolve(jsonResponse({ username: "admin" }));
    }
    return Promise.resolve(jsonResponse(allProducts));
  }));
}

describe("Mini-Mart storefront", () => {
  beforeEach(() => {
    localStorage.clear();
    mockFetch();
  });

  test("renders Mini-Mart branding and loads products", async () => {
    render(<App />);

    expect(screen.getByRole("heading", { name: "Mini-Mart" })).toBeInTheDocument();
    expect(await screen.findByText("Sparkling Water")).toBeInTheDocument();
    expect(screen.getByText("Dark Chocolate Bar")).toBeInTheDocument();
  });

  test("shows search input and accepts input", async () => {
    render(<App />);

    await screen.findByText("Sparkling Water");

    const searchInput = screen.getByPlaceholderText("Search products...");
    await userEvent.type(searchInput, "chocolate");

    expect(screen.getByDisplayValue("chocolate")).toBeInTheDocument();
  });

  test("category filter buttons work", async () => {
    render(<App />);

    await screen.findByText("Sparkling Water");

    await userEvent.click(screen.getByRole("button", { name: "Beverages" }));

    await waitFor(() => {
      expect(screen.queryByText("Dark Chocolate Bar")).not.toBeInTheDocument();
    });
    expect(screen.getByText("Sparkling Water")).toBeInTheDocument();
  });

  test("admin login page renders at /admin route", async () => {
    window.history.pushState({}, "", "/admin");

    render(<App />);

    expect(screen.getByText("Admin Login")).toBeInTheDocument();
    expect(screen.getByLabelText("Username")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
  });

  test("admin login form submits and shows console", async () => {
    window.history.pushState({}, "", "/admin");

    render(<App />);

    await userEvent.type(screen.getByLabelText("Username"), "admin");
    await userEvent.type(screen.getByLabelText("Password"), "admin123");
    await userEvent.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByText("Admin Console")).toBeInTheDocument();
  });

  test("admin console shows sign out button", async () => {
    localStorage.setItem("mini-mart-admin-token", "test-token");
    window.history.pushState({}, "", "/admin");

    render(<App />);

    expect(await screen.findByText("Admin Console")).toBeInTheDocument();
    expect(screen.getByText("Sign out")).toBeInTheDocument();
  });
});
