export default {
  methods: {
    syncUpdateFormModel(value) {
      if (this.designState) {
        return
      }

      if (this.subFormItemFlag) {
        let subFormData = this.formModel[this.subFormName] || [{}]
        let subFormDataRow = subFormData[this.subFormRowIndex]

        subFormDataRow[this.field.options.name] = value
      } else {
        this.formModel[this.field.options.name] = value
      }
    }

  }
}
