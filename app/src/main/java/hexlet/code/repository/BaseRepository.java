package hexlet.code.repository;

import javax.sql.DataSource;

public class BaseRepository {
    protected static DataSource dataSource;

    public static void setDataSource(DataSource newDataSource) {
        dataSource = newDataSource;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
