export default function once(target, event, handler, options) {
  if (typeof options === 'boolean') options = { capture: options }
  target.addEventListener(event, handler, { once: true, ...options })
}
