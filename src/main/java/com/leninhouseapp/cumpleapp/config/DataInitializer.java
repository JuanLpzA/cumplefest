package com.leninhouseapp.cumpleapp.config;

import com.leninhouseapp.cumpleapp.entity.Game;
import com.leninhouseapp.cumpleapp.entity.ImpostorWord;
import com.leninhouseapp.cumpleapp.entity.Question;
import com.leninhouseapp.cumpleapp.entity.User;
import com.leninhouseapp.cumpleapp.repository.GameRepository;
import com.leninhouseapp.cumpleapp.repository.ImpostorWordRepository;
import com.leninhouseapp.cumpleapp.repository.QuestionRepository;
import com.leninhouseapp.cumpleapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final GameRepository gameRepo;
    private final QuestionRepository questionRepo;
    private final ImpostorWordRepository impostorRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        initUsers();
        initGames();
        initQuestions();
        initImpostorWords();
    }

    private void initUsers() {
        if (userRepo.count() > 0) return;

        userRepo.saveAll(List.of(
                User.builder().username("diego").password(encoder.encode("123456")).role(User.Role.ADMIN).displayName("Diego").active(true).points(0).build(),
                User.builder().username("lenin").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Lenin").active(true).points(0).build(),
                User.builder().username("angelica").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Angélica").active(true).points(0).build(),
                User.builder().username("kiara").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Kiara").active(true).points(0).build(),
                User.builder().username("johan").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Johan").active(true).points(0).build(),
                User.builder().username("angel").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Angel").active(true).points(0).build(),
                User.builder().username("mary").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Mary").active(true).points(0).build(),
                User.builder().username("sergio").password(encoder.encode("123456")).role(User.Role.PLAYER).displayName("Sergio").active(true).points(0).build(),
                User.builder().username("television").password(encoder.encode("123456")).role(User.Role.TELEVISION).displayName("TV").active(true).points(0).build()
        ));
    }

    private void initGames() {
        if (gameRepo.count() > 0) return;

        gameRepo.saveAll(List.of(
                Game.builder()
                        .name("🏗️ Jenga")
                        .description("Torneo de Jenga eliminatorio. Se emparejan jugadores aleatoriamente en llaves y el ganador avanza a la siguiente ronda.")
                        .rules("Cada jugador saca una pieza por turno. El que tumbe la torre pierde. El ganador avanza en el torneo.")
                        .type(Game.GameType.JENGA_TOURNAMENT)
                        .status(Game.GameStatus.PENDING)
                        .pointsFirst(15).pointsSecond(10).pointsThird(6)
                        .hasMinigiftFirst(true).hasMinigiftSecond(false).hasMinigiftThird(false)
                        .orderIndex(1).gameData("{}").build(),

                Game.builder()
                        .name("🎭 Adivina el Personaje")
                        .description("Un jugador debe adivinar qué personaje es con preguntas de sí/no. Solo el admin registra el ganador.")
                        .rules("Se le pega un papel en la frente con un personaje. Haz preguntas de sí/no para adivinar quién eres. El primero en adivinar gana.")
                        .type(Game.GameType.GUESS_CHARACTER)
                        .status(Game.GameStatus.PENDING)
                        .pointsFirst(10).pointsSecond(6).pointsThird(3)
                        .hasMinigiftFirst(false).hasMinigiftSecond(false).hasMinigiftThird(false)
                        .orderIndex(2).gameData("{}").build(),

                Game.builder()
                        .name("❓ Preguntas Generales")
                        .description("El sistema elige 2 jugadores al azar para pelear. Se muestra una pregunta con 4 alternativas. El primero en presionar el buzzer responde.")
                        .rules("Cuando aparezca la pregunta, presiona BUZZER lo más rápido posible. Si respondes bien (el admin confirma), ganas puntos. Si fallas, el otro puede responder.")
                        .type(Game.GameType.TRIVIA)
                        .status(Game.GameStatus.PENDING)
                        .pointsFirst(8).pointsSecond(3).pointsThird(0)
                        .hasMinigiftFirst(false).hasMinigiftSecond(false).hasMinigiftThird(false)
                        .orderIndex(3).gameData("{\"currentQuestion\":null,\"player1\":null,\"player2\":null,\"buzzedBy\":null,\"phase\":\"IDLE\"}").build(),

                Game.builder()
                        .name("🕵️ El Impostor")
                        .description("Todos reciben una palabra secreta. El impostor recibe solo una pista. ¡Descubre quién es el impostor!")
                        .rules("Cada jugador describe la palabra brevemente. El impostor intenta pasar desapercibido. Al votar, si el impostor no es descubierto, gana puntos. Cantidad de impostores configurable.")
                        .type(Game.GameType.IMPOSTOR)
                        .status(Game.GameStatus.PENDING)
                        .pointsFirst(5).pointsSecond(3).pointsThird(0)
                        .hasMinigiftFirst(false).hasMinigiftSecond(false).hasMinigiftThird(false)
                        .orderIndex(4).gameData("{\"impostorCount\":1,\"phase\":\"IDLE\"}").build(),

                Game.builder()
                        .name("🏦 Monopoly Rápido")
                        .description("20 minutos de Monopoly acelerado. Gana quien tenga más dinero y propiedades al sonar el tiempo.")
                        .rules("Reglas estándar de Monopoly pero con tiempo limitado de 20 minutos. Al terminar el tiempo, se cuenta el dinero + valor de propiedades. Top 1 gana miniregalo, top 2 y 3 suman puntos.")
                        .type(Game.GameType.BASIC)
                        .status(Game.GameStatus.PENDING)
                        .pointsFirst(0).pointsSecond(10).pointsThird(6)
                        .hasMinigiftFirst(true).hasMinigiftSecond(false).hasMinigiftThird(false)
                        .orderIndex(5).gameData("{}").build(),

                Game.builder()
                        .name("🃏 Poker Rápido")
                        .description("20 minutos de Texas Hold'em. El que tenga más fichas al final gana.")
                        .rules("Texas Hold'em estándar: cada jugador recibe 2 cartas. Se comparten 5 cartas en la mesa (flop, turn, river). Mejor mano de 5 cartas gana. Duración: 20 minutos.")
                        .type(Game.GameType.BASIC)
                        .status(Game.GameStatus.PENDING)
                        .pointsFirst(0).pointsSecond(10).pointsThird(6)
                        .hasMinigiftFirst(true).hasMinigiftSecond(false).hasMinigiftThird(false)
                        .orderIndex(6).gameData("{}").build()
        ));
    }

    private void initQuestions() {
        if (questionRepo.count() > 0) return;

        questionRepo.saveAll(List.of(
                Question.builder().text("¿Cuál es la capital del Perú?").optionA("Cusco").optionB("Lima").optionC("Arequipa").optionD("Trujillo").correctAnswer("B").build(),
                Question.builder().text("¿En qué año se independizó el Perú?").optionA("1810").optionB("1824").optionC("1821").optionD("1819").correctAnswer("C").build(),
                Question.builder().text("¿Cuál es el plato nacional del Perú?").optionA("Lomo saltado").optionB("Ceviche").optionC("Ají de gallina").optionD("Anticuchos").correctAnswer("B").build(),
                Question.builder().text("¿Qué río es el más largo del mundo?").optionA("Nilo").optionB("Amazonas").optionC("Yangtsé").optionD("Mississippi").correctAnswer("A").build(),
                Question.builder().text("¿Cuántos planetas tiene el sistema solar?").optionA("7").optionB("9").optionC("8").optionD("10").correctAnswer("C").build(),
                Question.builder().text("¿En qué continente está Egipto?").optionA("Asia").optionB("Europa").optionC("África").optionD("Medio Oriente").correctAnswer("C").build(),
                Question.builder().text("¿Cuál es el animal más rápido del mundo?").optionA("León").optionB("Guepardo").optionC("Águila").optionD("Halcón peregrino").correctAnswer("D").build(),
                Question.builder().text("¿Cuántos huesos tiene el cuerpo humano adulto?").optionA("206").optionB("300").optionC("150").optionD("250").correctAnswer("A").build(),
                Question.builder().text("¿Cuál es la moneda del Perú?").optionA("Peso").optionB("Dólar").optionC("Sol").optionD("Bolívar").correctAnswer("C").build(),
                Question.builder().text("¿Quién pintó la Mona Lisa?").optionA("Miguel Ángel").optionB("Van Gogh").optionC("Picasso").optionD("Leonardo da Vinci").correctAnswer("D").build(),
                Question.builder().text("¿Cuál es la bebida bandera del Perú?").optionA("Chicha morada").optionB("Inca Kola").optionC("Pisco sour").optionD("Mate de coca").correctAnswer("C").build(),
                Question.builder().text("¿En qué país nació Shakira?").optionA("Venezuela").optionB("México").optionC("Colombia").optionD("Argentina").correctAnswer("C").build(),
                Question.builder().text("¿Cuántos lados tiene un hexágono?").optionA("5").optionB("6").optionC("7").optionD("8").correctAnswer("B").build(),
                Question.builder().text("¿Cuál es el país más grande del mundo?").optionA("Canadá").optionB("China").optionC("EE.UU.").optionD("Rusia").correctAnswer("D").build(),
                Question.builder().text("¿Qué significa CPU?").optionA("Central Processing Unit").optionB("Computer Power Unit").optionC("Central Power Unit").optionD("Computer Processing Unit").correctAnswer("A").build(),
                Question.builder().text("¿Cuántos departamentos tiene el Perú?").optionA("22").optionB("24").optionC("25").optionD("26").correctAnswer("C").build(),
                Question.builder().text("¿Cuál es la ciudad más poblada del mundo?").optionA("Pekín").optionB("Nueva York").optionC("Tokio").optionD("Shanghai").correctAnswer("C").build(),
                Question.builder().text("¿Qué deporte practica Lionel Messi?").optionA("Tenis").optionB("Fútbol").optionC("Basketball").optionD("Béisbol").correctAnswer("B").build(),
                Question.builder().text("¿Cuánto es 15 x 15?").optionA("200").optionB("215").optionC("225").optionD("220").correctAnswer("C").build(),
                Question.builder().text("¿Cuál es el océano más grande?").optionA("Atlántico").optionB("Índico").optionC("Ártico").optionD("Pacífico").correctAnswer("D").build()
        ));
    }

    private void initImpostorWords() {
        if (impostorRepo.count() > 0) return;

        impostorRepo.saveAll(List.of(
                ImpostorWord.builder().word("Ceviche").hint("Plato con limón").category("Comida").build(),
                ImpostorWord.builder().word("Pizza").hint("Comida italiana").category("Comida").build(),
                ImpostorWord.builder().word("Televisión").hint("Pantalla grande").category("Electrodoméstico").build(),
                ImpostorWord.builder().word("Refrigeradora").hint("Guarda cosas frías").category("Electrodoméstico").build(),
                ImpostorWord.builder().word("Zapatillas").hint("Las usas en los pies").category("Ropa").build(),
                ImpostorWord.builder().word("Casaca").hint("Prenda de abrigo").category("Ropa").build(),
                ImpostorWord.builder().word("Aretes").hint("Se usan en las orejas").category("Accesorio").build(),
                ImpostorWord.builder().word("Reloj").hint("Tiene manecillas").category("Accesorio").build(),
                ImpostorWord.builder().word("Mango").hint("Fruta tropical").category("Fruta").build(),
                ImpostorWord.builder().word("Piña").hint("Fruta con corona").category("Fruta").build(),
                ImpostorWord.builder().word("Guitarra").hint("Instrumento de cuerdas").category("Instrumento").build(),
                ImpostorWord.builder().word("Chicharrón").hint("Carne frita crocante").category("Comida").build(),
                ImpostorWord.builder().word("Silla").hint("Para sentarse").category("Mueble").build(),
                ImpostorWord.builder().word("Espejo").hint("Te muestra tu reflejo").category("Objeto").build(),
                ImpostorWord.builder().word("Llave").hint("Abre puertas").category("Objeto").build()
        ));
    }
}
