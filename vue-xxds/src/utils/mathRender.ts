import katex from 'katex'
import 'katex/dist/katex.min.css'

export function renderMath(latexString: string): string {
  if (!latexString) return ''
  // 处理块级公式 $$...$$
  let rendered = latexString.replace(/\$\$(.*?)\$\$/g, (match, tex) => {
    try {
      return katex.renderToString(tex, { displayMode: true })
    } catch (e) {
      console.error(e)
      return match
    }
  })
  // 处理行内公式 $...$
  rendered = rendered.replace(/\$(.*?)\$/g, (match, tex) => {
    try {
      return katex.renderToString(tex, { displayMode: false })
    } catch (e) {
      console.error(e)
      return match
    }
  })
  return rendered
}