@import hexlet.code.dto.UrlDto
@import java.util.List
@import java.time.format.DateTimeFormatter

@param List<UrlDto> urls
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
            <td>${url.getLastCheckCreatedAt() == null ? "" : url.getLastCheckCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}</td>
            <td>${url.getLastStatusCode() == null ? "" : url.getLastStatusCode().toString()}</td>
          </tr>
        @endfor
      </tbody>
    </table>
  `
)
