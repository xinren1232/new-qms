import { ref } from 'vue'

// 获取筛选条件已选了几个
const filterNumber = ref(0)

export default function useFilterNumber() {

  const setFilterNumber = (form) => {
    let number = 0

    for (let key in form) {
      if (form[key] !== '') {
        number++
      }
    }
    filterNumber.value = number
    console.log('filterNumber', filterNumber.value)
  }

  return {
    filterNumber,
    setFilterNumber
  }
}
