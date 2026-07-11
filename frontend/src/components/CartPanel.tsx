import { type CartItem } from "../api";

type CartPanelProps = {
  items: CartItem[];
  itemCount: number;
  open: boolean;
  onClose: () => void;
  onUpdateQuantity: (itemId: number, quantity: number) => void;
  onRemoveItem: (itemId: number) => void;
  error?: string;
};

export default function CartPanel({
  items,
  itemCount,
  open,
  onClose,
  onUpdateQuantity,
  onRemoveItem,
  error,
}: CartPanelProps) {
  const total = items.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0,
  );

  return (
    <>
      {open && (
        <div
          className="fixed inset-0 z-40 bg-black/20"
          onClick={onClose}
        />
      )}

      <div
        className={`fixed right-0 top-0 z-50 flex h-full w-80 flex-col border-l border-slate-200 bg-white shadow-lg transition-transform duration-300 ${
          open ? "translate-x-0" : "translate-x-full"
        }`}
      >
        <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-900">
            Cart ({itemCount})
          </h2>
          <button
            onClick={onClose}
            className="text-sm text-slate-400 hover:text-slate-900 transition-colors"
          >
            Close
          </button>
        </div>

        {error && (
          <div className="mx-4 mt-2 rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600">
            {error}
          </div>
        )}

        <div className="flex-1 overflow-y-auto px-4 py-4">
          {items.length === 0 ? (
            <p className="text-sm text-slate-400">Your cart is empty.</p>
          ) : (
            <ul className="flex flex-col gap-4">
              {items.map((item) => (
                <li key={item.id} className="flex gap-3">
                  <div className="h-16 w-16 flex-shrink-0 overflow-hidden bg-slate-50">
                    <img
                      src={item.product.imageUrl}
                      alt={item.product.name}
                      className="h-full w-full object-cover"
                    />
                  </div>
                  <div className="flex flex-1 flex-col justify-between">
                    <div>
                      <h3 className="text-sm font-medium text-slate-900">
                        {item.product.name}
                      </h3>
                      <p className="text-xs text-slate-400">
                        ${item.product.price.toFixed(2)} each
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() =>
                          onUpdateQuantity(item.id, item.quantity - 1)
                        }
                        className="flex h-6 w-6 items-center justify-center border border-slate-200 text-xs text-slate-600 hover:bg-slate-50 transition-colors"
                      >
                        -
                      </button>
                      <span className="w-6 text-center text-sm text-slate-900">
                        {item.quantity}
                      </span>
                      <button
                        onClick={() =>
                          onUpdateQuantity(item.id, item.quantity + 1)
                        }
                        className="flex h-6 w-6 items-center justify-center border border-slate-200 text-xs text-slate-600 hover:bg-slate-50 transition-colors"
                      >
                        +
                      </button>
                      <button
                        onClick={() => onRemoveItem(item.id)}
                        className="ml-auto text-xs text-slate-400 hover:text-red-500 transition-colors"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="border-t border-slate-200 px-4 py-3">
          <div className="flex items-center justify-between text-sm font-semibold text-slate-900">
            <span>Total</span>
            <span>${total.toFixed(2)}</span>
          </div>
        </div>
      </div>
    </>
  );
}
