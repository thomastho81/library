package br.com.thomas.library.search_service.seed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dados dos 60 livros de seed: mesmos livros e quantidades do catalog-service e inventory-service.
 * Usado pelo {@link br.com.thomas.library.search_service.config.ElasticsearchSeedRunner} para popular o índice na primeira subida.
 * Datas em LocalDateTime no padrão yyyy-MM-dd'T'HH:mm:ss para inserção correta no Elasticsearch.
 */
public final class SeedBooks {

    /** Data/hora padrão do seed (yyyy-MM-dd'T'HH:mm:ss). */
    private static final LocalDateTime SEED_DATETIME = LocalDateTime.of(2024, 6, 1, 12, 0, 0);

    public record SeedBook(
            int id,
            String title,
            String author,
            String category,
            String genre,
            String description,
            String isbn,
            int publishedYear,
            int totalCopies,
            int availableCopies,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime inventoryUpdatedAt
    ) {}

    /** 60 livros: id 1..60, totalCopies/availableCopies conforme inventory-service data.sql (copias_reservadas=0). */
    public static final List<SeedBook> ALL = List.of(
            new SeedBook(1, "1984", "George Orwell", "Ficção Científica", "Distopia", "Romance distópico sobre totalitarismo e vigilância.", "978-8525402136", 1949, 12, 12, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(2, "O Senhor dos Anéis", "J.R.R. Tolkien", "Fantasia", "Fantasia Épica", "Trilogia épica na Terra-média.", "978-8533613379", 1954, 45, 45, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(3, "Dom Quixote", "Miguel de Cervantes", "Romance", "Romance Clássico", "As aventuras do cavaleiro da triste figura.", "978-8525405014", 1605, 3, 3, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(4, "Cem Anos de Solidão", "Gabriel García Márquez", "Ficção", "Realismo Mágico", "Saga da família Buendía em Macondo.", "978-8528610761", 1967, 28, 28, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(5, "Harry Potter e a Pedra Filosofal", "J.K. Rowling", "Fantasia", "Fantasia Infantojuvenil", "O início da saga do jovem bruxo.", "978-8532511010", 1997, 50, 50, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(6, "O Pequeno Príncipe", "Antoine de Saint-Exupéry", "Infantil", "Fábula", "O principezinho que viaja por planetas.", "978-8595081512", 1943, 7, 7, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(7, "Orgulho e Preconceito", "Jane Austen", "Romance", "Romance Histórico", "Elizabeth Bennet e Mr. Darcy na Inglaterra rural.", "978-8525403478", 1813, 33, 33, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(8, "O Hobbit", "J.R.R. Tolkien", "Fantasia", "Fantasia Épica", "Bilbo Bolseiro e a jornada até a Montanha Solitária.", "978-8533613492", 1937, 19, 19, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(9, "Duna", "Frank Herbert", "Ficção Científica", "Space Opera", "Arrakis, especiaria e o messias Paul Atreides.", "978-8576572008", 1965, 41, 41, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(10, "Crime e Castigo", "Fiódor Dostoiévski", "Romance", "Romance Psicológico", "Raskólnikov e o assassinato da velha.", "978-8525405120", 1866, 0, 0, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(11, "O Nome da Rosa", "Umberto Eco", "Romance", "Romance Histórico", "Mistério em um mosteiro medieval.", "978-8525405236", 1980, 22, 22, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(12, "O Apanhador no Campo de Centeio", "J.D. Salinger", "Romance", "Romance de Formação", "Holden Caulfield em Nova York.", "978-8525405343", 1951, 8, 8, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(13, "O Guia do Mochileiro das Galáxias", "Douglas Adams", "Ficção Científica", "Comédia de Ficção Científica", "Arthur Dent e a resposta para a vida, o universo e tudo mais.", "978-8525405450", 1979, 15, 15, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(14, "Fundação", "Isaac Asimov", "Ficção Científica", "Space Opera", "O Império Galáctico e a Fundação de Hari Seldon.", "978-8525405567", 1951, 38, 38, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(15, "O Sol é para Todos", "Harper Lee", "Romance", "Romance Social", "Atticus Finch e o julgamento no Sul dos EUA.", "978-8525405674", 1960, 44, 44, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(16, "Neuromancer", "William Gibson", "Ficção Científica", "Cyberpunk", "Case e a matriz no futuro da informação.", "978-8525405781", 1984, 6, 6, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(17, "O Estrangeiro", "Albert Camus", "Romance", "Romance Existencialista", "Meursault e o absurdo da existência.", "978-8525405898", 1942, 31, 31, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(18, "A Revolução dos Bichos", "George Orwell", "Ficção", "Alegoria Política", "Os animais da granja tomam o poder.", "978-8525405904", 1945, 27, 27, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(19, "O Grande Gatsby", "F. Scott Fitzgerald", "Romance", "Romance Americano", "Gatsby e o sonho americano nos anos 20.", "978-8525406011", 1925, 11, 11, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(20, "Ensaio sobre a Cegueira", "José Saramago", "Ficção", "Romance Alegórico", "Uma epidemia de cegueira branca.", "978-8525406128", 1995, 49, 49, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(21, "O Alquimista", "Paulo Coelho", "Romance", "Romance de Descoberta", "O pastor Santiago e sua lenda pessoal.", "978-8525406235", 1988, 4, 4, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(22, "Memórias Póstumas de Brás Cubas", "Machado de Assis", "Romance", "Romance Realista", "Narrador defunto e a sociedade do Rio.", "978-8525406342", 1881, 36, 36, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(23, "O Retrato de Dorian Gray", "Oscar Wilde", "Romance", "Romance Gótico", "A beleza, o retrato e a decadência.", "978-8525406459", 1890, 17, 17, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(24, "It - A Coisa", "Stephen King", "Terror", "Terror Sobrenatural", "O palhaço Pennywise em Derry.", "978-8525406566", 1986, 25, 25, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(25, "O Iluminado", "Stephen King", "Terror", "Terror Psicológico", "Jack Torrance e o Overlook Hotel.", "978-8525406673", 1977, 42, 42, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(26, "Hyperion", "Dan Simmons", "Ficção Científica", "Space Opera", "Os peregrinos e o Shrike no planeta Hyperion.", "978-8525406780", 1989, 9, 9, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(27, "O Lobo da Estepe", "Hermann Hesse", "Romance", "Romance Existencialista", "Harry Haller entre o humano e o lobo.", "978-8525406897", 1927, 14, 14, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(28, "Sapiens", "Yuval Noah Harari", "Não Ficção", "História", "Uma breve história da humanidade.", "978-8525406903", 2011, 21, 21, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(29, "Cosmos", "Carl Sagan", "Não Ficção", "Divulgação Científica", "Viagem pelo universo e pela ciência.", "978-8525407010", 1980, 30, 30, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(30, "O Mundo de Sofia", "Jostein Gaarder", "Ficção", "Romance Filosófico", "Sofia e a história da filosofia.", "978-8525407127", 1991, 1, 1, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(31, "A Menina que Roubava Livros", "Markus Zusak", "Romance", "Romance Histórico", "Liesel na Alemanha nazista.", "978-8525407234", 2005, 39, 39, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(32, "O Código Da Vinci", "Dan Brown", "Suspense", "Thriller", "Robert Langdon e o segredo do Graal.", "978-8525407341", 2003, 23, 23, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(33, "As Crônicas de Nárnia", "C.S. Lewis", "Fantasia", "Fantasia Infantojuvenil", "O guarda-roupa e o mundo de Nárnia.", "978-8525407458", 1950, 47, 47, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(34, "O Senhor das Moscas", "William Golding", "Ficção", "Romance Alegórico", "Crianças numa ilha e a queda na selvageria.", "978-8525407565", 1954, 5, 5, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(35, "Fahrenheit 451", "Ray Bradbury", "Ficção Científica", "Distopia", "Bombeiros que queimam livros.", "978-8525407672", 1953, 18, 18, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(36, "O Processo", "Franz Kafka", "Romance", "Romance Existencialista", "Joseph K. e o processo sem sentido.", "978-8525407789", 1925, 34, 34, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(37, "A Metamorfose", "Franz Kafka", "Ficção", "Romance Alegórico", "Gregor Samsa acorda transformado em inseto.", "978-8525407896", 1915, 26, 26, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(38, "Moby Dick", "Herman Melville", "Romance", "Romance de Aventura", "Capitão Ahab e a caça à baleia branca.", "978-8525407902", 1851, 43, 43, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(39, "Ulisses", "James Joyce", "Romance", "Romance Modernista", "Um dia na vida de Leopold Bloom em Dublin.", "978-8525408019", 1922, 10, 10, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(40, "A Casa dos Espíritos", "Isabel Allende", "Romance", "Realismo Mágico", "A família Trueba no Chile.", "978-8525408126", 1982, 37, 37, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(41, "O Amor nos Tempos do Cólera", "Gabriel García Márquez", "Romance", "Realismo Mágico", "Florentino e Fermina ao longo das décadas.", "978-8525408233", 1985, 2, 2, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(42, "O Príncipe", "Nicolau Maquiavel", "Não Ficção", "Ciência Política", "Reflexões sobre poder e governo.", "978-8525408340", 1532, 29, 29, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(43, "O Diário de Anne Frank", "Anne Frank", "Não Ficção", "Memórias", "O diário no esconderijo em Amsterdã.", "978-8525408457", 1947, 48, 48, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(44, "Sobre a Brevidade da Vida", "Sêneca", "Não Ficção", "Filosofia", "Cartas sobre o uso do tempo.", "978-8525408564", 49, 16, 16, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(45, "O Conde de Monte Cristo", "Alexandre Dumas", "Romance", "Romance de Aventura", "Edmond Dantès e a vingança.", "978-8525408671", 1844, 35, 35, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(46, "Os Três Mosqueteiros", "Alexandre Dumas", "Romance", "Romance de Aventura", "D'Artagnan, Athos, Porthos e Aramis.", "978-8525408788", 1844, 40, 40, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(47, "O Médico e o Monstro", "Robert Louis Stevenson", "Ficção", "Romance Gótico", "Dr. Jekyll e Mr. Hyde.", "978-8525408895", 1886, 13, 13, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(48, "Drácula", "Bram Stoker", "Terror", "Terror Gótico", "O conde vampiro na Inglaterra vitoriana.", "978-8525408901", 1897, 24, 24, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(49, "Frankenstein", "Mary Shelley", "Ficção Científica", "Terror Gótico", "Victor Frankenstein e a criatura.", "978-8525409018", 1818, 46, 46, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(50, "O Silmarillion", "J.R.R. Tolkien", "Fantasia", "Fantasia Épica", "A criação da Terra-média e as Silmarils.", "978-8525409125", 1977, 20, 20, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(51, "O Leão, a Feiticeira e o Guarda-Roupa", "C.S. Lewis", "Fantasia", "Fantasia Infantojuvenil", "Primeiro livro de Nárnia.", "978-8525409232", 1950, 32, 32, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(52, "O Cemitério", "Stephen King", "Terror", "Terror Sobrenatural", "Louis Creed e o cemitério dos animais.", "978-8525409349", 1983, 28, 28, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(53, "Carrie, a Estranha", "Stephen King", "Terror", "Terror Sobrenatural", "Carrie White e o baile de formatura.", "978-8525409456", 1974, 50, 50, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(54, "Clean Code", "Robert C. Martin", "Técnico", "Programação", "Arte de código limpo em software.", "978-8535212501", 2008, 7, 7, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(55, "Design Patterns", "Gang of Four", "Técnico", "Programação", "Padrões de projeto orientados a objetos.", "978-8535212502", 1994, 33, 33, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(56, "Introdução aos Algoritmos", "Cormen et al.", "Técnico", "Ciência da Computação", "CLRS: algoritmos e estruturas de dados.", "978-8535212503", 2009, 19, 19, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(57, "O Canto da Sereia", "Gillian Flynn", "Suspense", "Thriller Psicológico", "Nick e Amy Dunne e o desaparecimento.", "978-8525409563", 2012, 41, 41, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(58, "O Silêncio dos Inocentes", "Thomas Harris", "Suspense", "Thriller Policial", "Clarice Starling e Hannibal Lecter.", "978-8525409670", 1988, 0, 0, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(59, "And Then There Were None", "Agatha Christie", "Policial", "Mistério", "Dez estranhos numa ilha.", "978-8525409787", 1939, 22, 22, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME),
            new SeedBook(60, "Assassinato no Expresso do Oriente", "Agatha Christie", "Policial", "Mistério", "Poirot no trem e o assassinato.", "978-8525409894", 1934, 8, 8, SEED_DATETIME, SEED_DATETIME, SEED_DATETIME)
    );

    private SeedBooks() {}
}
