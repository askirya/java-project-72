@import hexlet.code.model.Url
@import java.util.List

@param List<Url> urls
@param String flash = null
@param String flashType = "info"

@template.layout(
  title = "Сайты",
  flash = flash,
  flashType = flashType,
  content = @`
    <h1>Сайты</h1>
    <table class="table table-bordered" data-test="urls">
      <thead>
        <tr>
          <th>ID</th>
          <th>Имя</th>
          <th>Последняя проверка</th>
          <th>Код ответа</th>
        </tr>
      </thead>
      <tbody>
        @for(var url : urls)
          <tr>
            <td>${url.getId()}</td>
            <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
            <td>${url.getLastCheckCreatedAt() == null ? "" : url.getLastCheckCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
            <td>${url.getLastStatusCode() == null ? "" : url.getLastStatusCode().toString()}</td>
          </tr>
        @endfor
      </tbody>
    </table>
  `
)
