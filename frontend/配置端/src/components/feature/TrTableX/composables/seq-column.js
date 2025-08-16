import { watch } from 'vue'

export const SeqColumn = { type: 'seq', label: 'No.', prop: 'seq', align: 'center', fixed: 'left', width: 50 }

export const useSeqColumn = (props) => {
  const { seqColumn: seq } = props

  watch(
    () => props.columns,
    (columns) => {
      if (seq && columns.length && !columns.find(({ prop }) => prop === SeqColumn.prop)) {
        columns.unshift(SeqColumn)
      }
    },
    { immediate: true }
  )
}
