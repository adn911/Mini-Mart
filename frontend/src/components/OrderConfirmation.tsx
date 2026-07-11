import { type OrderResponse } from "../api";

type OrderConfirmationProps = {
  order: OrderResponse;
  onContinueShopping: () => void;
};

export default function OrderConfirmation({ order, onContinueShopping }: OrderConfirmationProps) {
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