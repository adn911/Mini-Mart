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

describe("Mini-Mart storefront", () => {
  beforeEach(() => {
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
      return Promise.resolve(jsonResponse(allProducts));
    }));
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
});
