@import gg.jte.Content

@param String title
@param Content content
@param String flash = null

<!doctype html>
<html lang="ru">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
      rel="stylesheet"
    >
  </head>
  <body class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <div class="container">
        <a class="navbar-brand" href="/">Анализатор страниц</a>
        <div class="navbar-nav">
          <a class="nav-link" href="/urls">Сайты</a>
        </div>
      </div>
    </nav>
    <main class="container flex-grow-1 py-4">
      @if (flash != null && !flash.isBlank())
        <div class="alert alert-info" role="alert">${flash}</div>
      @endif
      ${content}
    </main>
    <footer class="border-top py-3">
      <div class="container text-center">
        <a href="https://ru.hexlet.io/">Hexlet</a>
      </div>
    </footer>
  </body>
</html>
