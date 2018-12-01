package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.List;



@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {

        SpringApplication.run(SalvoApplication.class, args);

    }

    @Bean
    public CommandLineRunner init(PlayerRepository playersRepository,
                                  GameRepository gamesRepository,
                                  GamePlayerRepository gamePlayersRepository,
                                  ShipRepository shipRepository,
                                  SalvoRepository salvoRepository,
                                  ScoreRepository scoreRepository) {
        return (args) -> {

            Player p1 = new Player("j.bauer@ctu.gov","24");
            Player p2 = new Player("c.obrian@ctu.gov","42");
            Player p3 = new Player("kim_bauer@gmail.com","kb");
            Player p4 = new Player("t.almeida@ctu.gov","mole");
            Player p5 = new Player("tareq@gmail.com","74");
            playersRepository.save(p1);
            playersRepository.save(p2);
            playersRepository.save(p3);
            playersRepository.save(p4);
            playersRepository.save(p5);
            Game g1 = new Game();
            Game g2 = new Game();
            Game g3 = new Game();
            Game g4 = new Game();
            Game g5 = new Game();
            Game g6 = new Game();

            gamesRepository.save(g1);
            gamesRepository.save(g2);
            gamesRepository.save(g3);
            gamesRepository.save(g4);
            gamesRepository.save(g5);
            gamesRepository.save(g6);
//
            GamePlayer gp1 = new GamePlayer(p1, g1);
            GamePlayer gp2 = new GamePlayer(p2, g1);
            GamePlayer gp3 = new GamePlayer(p1, g2);
            GamePlayer gp4 = new GamePlayer(p2, g2);
            GamePlayer gp5 = new GamePlayer(p3, g3);
            GamePlayer gp6 = new GamePlayer(p4, g3);
            GamePlayer gp7 = new GamePlayer(p3, g4);
            GamePlayer gp8 = new GamePlayer(p4, g4);
            GamePlayer gp9 = new GamePlayer(p4, g5);
            GamePlayer gp10 = new GamePlayer(p1, g5);
            GamePlayer gp11 = new GamePlayer(p5, g6);
            gamePlayersRepository.save(gp1);
            gamePlayersRepository.save(gp2);
            gamePlayersRepository.save(gp3);
            gamePlayersRepository.save(gp4);
            gamePlayersRepository.save(gp5);
            gamePlayersRepository.save(gp6);
            gamePlayersRepository.save(gp7);
            gamePlayersRepository.save(gp8);
            gamePlayersRepository.save(gp9);
            gamePlayersRepository.save(gp10);
            gamePlayersRepository.save(gp11);

            List<String> location1 = Arrays.asList("E1", "F1", "G1");
            Ship ship1 = new Ship("Submarine", location1, gp1);
            shipRepository.save(ship1);

            List<String> location2 = Arrays.asList("H2", "H3", "H4");
            Ship ship2 = new Ship("destroyer", location2, gp1);
            shipRepository.save(ship2);

            List<String> location3 = Arrays.asList("F1","F2");
            Ship ship3 = new Ship("Patrol Boat", location3, gp2);
            shipRepository.save(ship3);
                    List<String> location4 = Arrays.asList("B5", "C5", "D5");
            Ship ship4 = new Ship("destroyer", location4, gp2);
            shipRepository.save(ship4);


        List<String> sl1 = Arrays.asList("F1", "A1");
        Salvo salvo1 = new Salvo(1,sl1,gp1);

        List<String> sl2 = Arrays.asList("A3", "B4");
        Salvo salvo2 = new Salvo(1, sl2, gp2);
            List<String> sl3 = Arrays.asList("C4", "H3");
            Salvo salvo3 = new Salvo(2,sl3,gp1);

            List<String> sl4 = Arrays.asList("C5", "D7");
            Salvo salvo4 = new Salvo(2, sl4, gp2);


            salvoRepository.save(salvo1);
        salvoRepository.save(salvo2);
            salvoRepository.save(salvo3);
            salvoRepository.save(salvo4);

            Score sc1 = new Score(p1,g1,1.0);
            Score sc2 = new Score(p2,g1,0.0);
            Score sc3 = new Score(p1,g2,0.5);
            Score sc4 = new Score(p2,g2,0.5);
            Score sc5 = new Score(p3,g3,1.0);
            Score sc6 = new Score(p4,g3,0.0);
            Score sc7 = new Score(p3,g4,1.0);
            Score sc8 = new Score(p4,g4,0.0);

            scoreRepository.save(sc1);
            scoreRepository.save(sc2);
            scoreRepository.save(sc3);
            scoreRepository.save(sc4);
            scoreRepository.save(sc5);
            scoreRepository.save(sc6);
            scoreRepository.save(sc7);
            scoreRepository.save(sc8);
        };
    }
}
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(inputName-> {
            Player player = playerRepository.findByUserName(inputName);
            if (player != null) {
                return new User(player.getUserName(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}


@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()

                .antMatchers("/**").permitAll()
                .antMatchers("/rest/**").denyAll()
                .and()
                .formLogin();

        http.formLogin()
                .usernameParameter("userName")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");
        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}





