@import hexlet.code.model.Url

@param Url url
@param String flash = null

@template.layout(
  title = "Сайт: " + url.getName(),
  flash = flash,
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
  `
)
