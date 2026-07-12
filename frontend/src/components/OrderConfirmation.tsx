import { type CartItem, type OrderResponse } from "../api";

type OrderConfirmationProps = {
  items?: CartItem[];
  total?: number;
  order?: OrderResponse;
  onConfirmOrder?: () => void;
  onGoBack?: () => void;
  onContinueShopping?: () => void;
  confirming?: boolean;
  error?: string;
};

export default function OrderConfirmation({
  items = [],
  total = 0,
  order,
  onConfirmOrder,
  onGoBack,
  onContinueShopping,
  confirming = false,
  error,
}: OrderConfirmationProps) {
  if (order) {
    return (
      <div className="mx-auto max-w-2xl px-6 py-12">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
            <svg className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">Order Confirmed</h1>
          <p className="mt-1 text-sm text-slate-500">Thank you for your purchase!</p>
        </div>

        <div className="rounded-lg border border-slate-200">
          <div className="border-b border-slate-200 px-6 py-4">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-slate-900">Order #{order.id}</span>
              <span className="text-xs uppercase tracking-wider text-slate-500">{order.status}</span>
            </div>
          </div>

          <div className="border-b border-slate-200 px-6 py-4">
            <h3 className="mb-2 text-xs font-medium uppercase tracking-wider text-slate-500">Items</h3>
            <ul className="divide-y divide-slate-100">
              {order.items.map((item) => (
                <li key={item.id} className="flex items-center gap-3 py-2">
                  <div className="h-12 w-12 flex-shrink-0 overflow-hidden rounded bg-slate-50">
                    <img src={item.product.imageUrl} alt={item.product.name}
                      className="h-full w-full object-cover" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-slate-900">{item.product.name}</p>
                    <p className="text-xs text-slate-400">Qty: {item.quantity} &times; ${item.unitPrice.toFixed(2)}</p>
                  </div>
                  <p className="text-sm font-medium text-slate-900">${(item.quantity * item.unitPrice).toFixed(2)}</p>
                </li>
              ))}
            </ul>
          </div>

          <div className="border-b border-slate-200 px-6 py-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-500">Payment Method</span>
              <span className="font-medium text-slate-900">Cash on Delivery</span>
            </div>
          </div>

          <div className="px-6 py-4">
            <div className="flex items-center justify-between text-base font-semibold text-slate-900">
              <span>Total</span>
              <span>${order.total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <div className="mt-6 text-center">
          <button onClick={onContinueShopping}
            className="bg-slate-900 px-6 py-2.5 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90">
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl px-6 py-12">
      <h1 className="mb-8 text-center text-2xl font-semibold tracking-tight text-slate-900">Review Your Order</h1>

      <div className="rounded-lg border border-slate-200">
        <div className="border-b border-slate-200 px-6 py-4">
          <h3 className="text-xs font-medium uppercase tracking-wider text-slate-500">Items ({items.length})</h3>
        </div>

        <div className="px-6 py-4">
          <ul className="divide-y divide-slate-100">
            {items.map((item) => (
              <li key={item.id} className="flex items-center gap-3 py-2">
                <div className="h-12 w-12 flex-shrink-0 overflow-hidden rounded bg-slate-50">
                  <img src={item.product.imageUrl} alt={item.product.name}
                    className="h-full w-full object-cover" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-slate-900">{item.product.name}</p>
                  <p className="text-xs text-slate-400">Qty: {item.quantity} &times; ${item.product.price.toFixed(2)}</p>
                </div>
                <p className="text-sm font-medium text-slate-900">${(item.quantity * item.product.price).toFixed(2)}</p>
              </li>
            ))}
          </ul>
        </div>

        <div className="border-b border-slate-200 px-6 py-4">
          <div className="flex items-center justify-between text-sm">
            <span className="text-slate-500">Payment Method</span>
            <span className="font-medium text-slate-900">Cash on Delivery</span>
          </div>
        </div>

        <div className="px-6 py-4">
          <div className="flex items-center justify-between text-base font-semibold text-slate-900">
            <span>Total</span>
            <span>${total.toFixed(2)}</span>
          </div>
        </div>
      </div>

      {error && (
        <div className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600">
          {error}
        </div>
      )}

      <div className="mt-6 flex flex-col items-center gap-3">
        <button onClick={onConfirmOrder} disabled={confirming}
          className="w-full bg-slate-900 py-2.5 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90 disabled:opacity-50">
          {confirming ? "Placing Order..." : "Confirm Order"}
        </button>
        <button onClick={onGoBack}
          className="text-xs font-medium uppercase tracking-wider text-slate-500 transition-colors hover:text-slate-900">
          Go Back
        </button>
      </div>
    </div>
  );
}
