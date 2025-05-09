package logbo.assy.automa.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe gérant la connexion à la base de données.
 */
public class Database implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_DB = "automa";
    private static final String DEFAULT_USER = "automa";
    private static final String DEFAULT_PASSWORD = "automa";

    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    /**
     * Constructeur avec paramètres personnalisés.
     * Établit immédiatement la connexion à la base de données.
     *
     * @param host     Le nom d'hôte du serveur MySQL
     * @param dbName   Le nom de la base de données
     * @param username Le nom d'utilisateur
     * @param password Le mot de passe
     */
    public Database(String host, String dbName, String username, String password) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName +
                "?useSSL=true" +
                "&serverTimezone=UTC" +
                "&useUnicode=true" +
                "&characterEncoding=utf8mb4" +
                "&connectionCollation=utf8mb4_unicode_ci" +
                "&characterSetResults=utf8mb4";
        this.username = username;
        this.password = password;
        this.connection = initConnection();
    }

    /**
     * Constructeur par défaut utilisant les valeurs prédéfinies.
     */
    public Database() {
        this(DEFAULT_HOST, DEFAULT_DB, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    /**
     * Initialise une nouvelle connexion à la base de données.
     *
     * @return Une connexion active à la base de données
     */
    private Connection initConnection() {
        try {
            Class.forName(JDBC_DRIVER);
            LOGGER.log(Level.INFO, "Connexion à la base de données {0}", url);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC introuvable", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de connexion à la base de données", e);
        }
        return null;
    }

    /**
     * Obtient la connexion à la base de données.
     * Si la connexion est fermée, tente de la réinitialiser.
     *
     * @return La connexion active à la base de données
     * @throws SQLException Si la connexion est fermée et ne peut pas être rétablie
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOGGER.log(Level.INFO, "Reconnexion automatique à la base de données");
            connection = initConnection();
        }
        return connection;
    }

    /**
     * Ferme la connexion à la base de données.
     */
    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                LOGGER.log(Level.INFO, "Connexion fermée avec succès");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }
}



