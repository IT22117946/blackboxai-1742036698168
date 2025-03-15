package com.skillsharing.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void getCookie_WithExistingCookie_ShouldReturnCookie() {
        // Arrange
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Cookie cookie = new Cookie(cookieName, cookieValue);
        Cookie[] cookies = new Cookie[]{cookie};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        Optional<Cookie> result = CookieUtils.getCookie(request, cookieName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(cookieName, result.get().getName());
        assertEquals(cookieValue, result.get().getValue());
    }

    @Test
    void getCookie_WithNonExistingCookie_ShouldReturnEmpty() {
        // Arrange
        String cookieName = "nonExistingCookie";
        Cookie[] cookies = new Cookie[]{new Cookie("otherCookie", "value")};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        Optional<Cookie> result = CookieUtils.getCookie(request, cookieName);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getCookie_WithNullCookies_ShouldReturnEmpty() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        Optional<Cookie> result = CookieUtils.getCookie(request, "anyCookie");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void addCookie_ShouldAddCookieWithCorrectProperties() {
        // Act
        CookieUtils.addCookie(response, "testCookie", "testValue", 3600);

        // Assert
        verify(response).addCookie(argThat(cookie -> 
            cookie.getName().equals("testCookie") &&
            cookie.getValue().equals("testValue") &&
            cookie.getMaxAge() == 3600 &&
            cookie.getPath().equals("/") &&
            cookie.isHttpOnly()
        ));
    }

    @Test
    void deleteCookie_WithExistingCookie_ShouldDeleteCookie() {
        // Arrange
        String cookieName = "testCookie";
        Cookie cookie = new Cookie(cookieName, "testValue");
        Cookie[] cookies = new Cookie[]{cookie};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        CookieUtils.deleteCookie(request, response, cookieName);

        // Assert
        verify(response).addCookie(argThat(deletedCookie -> 
            deletedCookie.getName().equals(cookieName) &&
            deletedCookie.getValue().equals("") &&
            deletedCookie.getPath().equals("/") &&
            deletedCookie.getMaxAge() == 0
        ));
    }

    @Test
    void deleteCookie_WithNonExistingCookie_ShouldDoNothing() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        CookieUtils.deleteCookie(request, response, "nonExistingCookie");

        // Assert
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void serialize_DeserializeObject_ShouldMaintainObjectIntegrity() {
        // Arrange
        TestObject testObject = new TestObject("test", 123);
        Cookie cookie = new Cookie("testCookie", "");

        // Act
        String serialized = CookieUtils.serialize(testObject);
        cookie.setValue(serialized);
        TestObject deserialized = CookieUtils.deserialize(cookie, TestObject.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(testObject.getName(), deserialized.getName());
        assertEquals(testObject.getValue(), deserialized.getValue());
    }

    @Test
    void deserialize_WithInvalidData_ShouldThrowException() {
        // Arrange
        Cookie cookie = new Cookie("testCookie", "invalid-data");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            CookieUtils.deserialize(cookie, TestObject.class);
        });
    }

    // Test helper class
    private static class TestObject implements java.io.Serializable {
        private final String name;
        private final int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
