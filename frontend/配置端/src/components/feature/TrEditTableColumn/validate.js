export function validate(value, row, validators) {
  if (!validators) return ''
  for (let i = 0; i < validators.length; i++) {
    const validator = validators[i]
    let result = true

    if (isEmpty(value)) {
      if (validator.required) {
        result = false
      }
    } else {
      if (validator.min !== undefined || validator.max !== undefined) {
        if (typeof value !== 'string') break
        if (validator.min !== undefined) {
          if (value.length < validator.min) result = false
        }

        if (validator.max !== undefined) {
          if (value.length > validator.max) result = false
        }
      } else if (validator.pattern && !validator.pattern.test(value)) {
        result = false
      } else if (typeof validator.validator === 'function') {
        validator.message = validator.validator(value, row)
        result = !!validator.message
      }
    }

    if (!result) {
      return validator.message
    }
  }

  return ''
}

function isEmpty(value) {
  if (!value) return true

  return Array.isArray(value) && !value.length
}
