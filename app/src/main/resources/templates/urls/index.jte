@import hexlet.code.model.Url
@import java.util.List

@param List<Url> urls
@param String flash = null

@template.layout(
  title = "Сайты",
  flash = flash,
  content = @`
    <h1>Сайты</h1>
    <table class="table table-bordered" data-test="urls">
      <thead>
        <tr>
          <th>ID</th>
          <th>Имя</th>
          <th>Дата создания</th>
          <th>Код ответа</th>
        </tr>
      </thead>
      <tbody>
        @for(var url : urls)
          <tr>
            <td>${url.getId()}</td>
            <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
            <td>${url.getCreatedAt().toLocalDateTime().toLocalDate().toString()}</td>
            <td></td>
          </tr>
        @endfor
      </tbody>
    </table>
  `
)
