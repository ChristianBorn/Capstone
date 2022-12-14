package de.ffmjava.capstone.backend.user;

import de.ffmjava.capstone.backend.user.model.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository mockUserRepository = mock(UserRepository.class);

    private final BCryptPasswordEncoder mockPasswordEncoder = mock(BCryptPasswordEncoder.class);
    private final UserService userService = new UserService(mockUserRepository);


    @Test
    void saveSuccessful() {
        AppUser newAppUser = new AppUser("null", "username", "password", "", "", "email");
        newAppUser = newAppUser.withEMail("email@email.de").withUsername("Different username");
        when(mockUserRepository.save(newAppUser)).thenReturn(newAppUser);
        when(mockPasswordEncoder.encode("password")).thenReturn("encodedPassword");
        String actual = userService.save(newAppUser, mockPasswordEncoder);
        String expected = "Created user: " + newAppUser.username();

        assertEquals(expected, actual);
        verify(mockPasswordEncoder).encode("password");
    }


    @Test
    void findByUsername() {
        AppUser appUser = new AppUser("null", "username", "password", "", "", "email");
        when(mockUserRepository.findByUsername("username")).thenReturn(appUser);

        AppUser expected = appUser;
        AppUser actual = userService.findByUsername("username");

        assertEquals(expected, actual);
    }
}