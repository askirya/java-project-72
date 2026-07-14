@import hexlet.code.model.Url
@import hexlet.code.model.UrlCheck
@import java.util.List

@param Url url
@param List<UrlCheck> checks
@param String flash = null
@param String flashType = "info"

@template.layout(
  title = "Сайт: " + url.getName(),
  flash = flash,
  flashType = flashType,
  content = @`
    <h1>Сайт: ${url.getName()}</h1>
    <table class="table table-bordered" data-test="url">
      <tbody>
        <tr>
          <td>ID</td>
          <td>${url.getId()}</td>
        </tr>
        <tr>
          <td>Имя</td>
          <td>${url.getName()}</td>
        </tr>
        <tr>
          <td>Дата создания</td>
          <td>${url.getCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
        </tr>
      </tbody>
    </table>
    <h2>Проверки</h2>
    <form method="post" action="/urls/${url.getId()}/checks">
      <input type="submit" value="Запустить проверку">
    </form>
    <table class="table table-bordered mt-3" data-test="checks">
      <thead>
        <tr>
          <th>ID</th>
          <th>Код ответа</th>
          <th>h1</th>
          <th>title</th>
          <th>description</th>
          <th>Дата создания</th>
        </tr>
      </thead>
      <tbody>
        @for(var check : checks)
          <tr>
            <td>${check.getId()}</td>
            <td>${check.getStatusCode()}</td>
            <td>${check.getH1Preview()}</td>
            <td>${check.getTitlePreview()}</td>
            <td>${check.getDescriptionPreview()}</td>
            <td>${check.getCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
          </tr>
        @endfor
      </tbody>
    </table>
  `
)
