export async function loadDocxPreview() {
  const { default: docxPreview } = await import(
    /* webpackChunkName: "docx-preview" */ 'docx-preview'
  )

  return docxPreview
}
