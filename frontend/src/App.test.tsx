import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, test, vi } from "vitest";

import App from "./App";

describe("Mini-Mart app shell", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response(
      JSON.stringify({ status: "ok", service: "mini-mart-backend" }),
      {
        status: 200,
        headers: { "Content-Type": "application/json" }
      }
    )));
  });

  test("shows Mini-Mart and backend health", async () => {
    render(<App />);

    expect(screen.getByRole("heading", { name: "Mini-Mart" })).toBeInTheDocument();
    expect(await screen.findByText("Backend: mini-mart-backend is ok")).toBeInTheDocument();
  });
});
