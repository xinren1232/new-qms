import { ref } from 'vue'


export const collapseStatus = ref(true)

export const handleCollapse = () => {
  collapseStatus.value = !collapseStatus.value
}
