package mfb.pruebas.GestorLogin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GestorLoginTest {
    GestorLogin login;//SUT
    IRepositorioCuentas repo;//collaborator mock
    ICuenta cuenta;//collaborator
    ICuenta cuenta1;//collaborator
    
    @Before
    public void setUp() {
	repo=mock(IRepositorioCuentas.class);// verify (MOSCKITO)
	cuenta=mock(ICuenta.class);
	cuenta1=mock(ICuenta.class);
	when(repo.buscar("pepe")).thenReturn(cuenta);
	login= new GestorLogin(repo);//assert (JUNIT)
	
    }
    @After
    public void tearDown() {
	repo=null;
	cuenta=null;
	cuenta1=null;
	login=null;
    }

    @Test
    public void testAccesoConcedidoALaPrimera() {
	when(cuenta.claveCorrecta("1234")).thenReturn(true);
	login.acceder("pepe", "1234");
	verify(cuenta,times(1)).entrarCuenta();
	verify(cuenta,never()).bloquearCuenta();
    }
    @Test
    public void testAccesoDenegadoALaPrimera() {
	when(cuenta.claveCorrecta("1111")).thenReturn(false);
	login.acceder("pepe", "1111");
	verify(cuenta,never()).entrarCuenta();
    }
    @Test
    public void testAccesoDenegadoALaPrimera_NoBloqueada() {
	when(cuenta.claveCorrecta("1111")).thenReturn(false);
	login.acceder("pepe", "1111");
	verify(cuenta,never()).entrarCuenta();
	verify(cuenta,never()).bloquearCuenta();
	assertThat(login.getNumFallos(), is(1));
    }
    @Test(expected= ExcepcionUsuarioDesconocido.class)
    public void testUsuarioDesconocido() {
	when(repo.buscar("manolo")).thenThrow(ExcepcionUsuarioDesconocido.class);
	login.acceder("manolo", anyString());
    }
    @Test
    public void testUsuarioDesconocido2() {
	when(repo.buscar("manolo")).thenThrow(ExcepcionUsuarioDesconocido.class);
	try {
	login.acceder("manolo", anyString());
	fail("Debe lanzar excepción");
	}catch(ExcepcionUsuarioDesconocido e){
	    verify(repo).buscar("manolo");	    
	}
    }
    @Test
    public void testBloqueoTrasTresIntentos() {

	when(cuenta.claveCorrecta("1111")).thenReturn(false);
	login.acceder("pepe", "1234");
	login.acceder("pepe", "1234");
	login.acceder("pepe", "1234");
	assertThat(login.getNumFallos(), is(3));
	verify(cuenta,times(3)).estaBloqueada();
	verify(cuenta,times(1)).bloquearCuenta();
    }
    @Test
    public void testBloqueoTrasCuatroIntentos() {

	when(cuenta.claveCorrecta("1111")).thenReturn(false);
	login.acceder("pepe", "1234");
	login.acceder("pepe", "1234");
	login.acceder("pepe", "1234");
	login.acceder("pepe", "1234");
	assertThat(login.getNumFallos(), is(4));
	verify(cuenta,times(4)).estaBloqueada();
	verify(cuenta,times(1)).bloquearCuenta();
    }
    @Test
    public void testSeAccedeTrasUnFallo() {

	when(cuenta.claveCorrecta("1111")).thenReturn(true);
	login.acceder("pepe", "1234");
	assertThat(login.getNumFallos(), is(1));
	login.acceder("pepe", "1111");
	verify(cuenta).entrarCuenta();
	
    }
    @Test
    public void testSeAccedeTrasDosFallo() {

	when(cuenta.claveCorrecta("1111")).thenReturn(true);
	login.acceder("pepe", "1234");
	login.acceder("pepe", "1234");
	assertThat(login.getNumFallos(), is(2));
	login.acceder("pepe", "1111");
	verify(cuenta).entrarCuenta();
	
    }

    @Test
    public void testAccedeOtroUsuarioTrasBloqueo() {
	
	when(cuenta.estaBloqueada()).thenReturn(true);
	login.acceder("pepe", "1234");
	verify(cuenta,never()).entrarCuenta();
	verify(cuenta).estaBloqueada();

	when(repo.buscar("manolo")).thenReturn(cuenta1);//hay que buscar una nueva cuenta para casa usuario	
	when(cuenta1.claveCorrecta("1234")).thenReturn(true);
	login.acceder("manolo", "1234");
	verify(cuenta1).entrarCuenta();
	
    }
    @Test
    public void testSeDeniegaAccesoCuentasEnUso() {
	when(cuenta.claveCorrecta("1234")).thenReturn(true);
	login.acceder("pepe", "1234");
	verify(cuenta).entrarCuenta();
	login.acceder("pepe", "1234");
	when(cuenta.estaEnUso()).thenThrow(ExcepcionCuentaEnUso.class);
	
    }
    
    
    



}
