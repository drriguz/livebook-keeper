document.addEventListener('DOMContentLoaded', (event) => {
  document.querySelectorAll('pre').forEach((block) => {
    hljs.highlightBlock(block);
  });
});