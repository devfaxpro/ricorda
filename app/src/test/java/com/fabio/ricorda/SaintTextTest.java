package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
public class SaintTextTest {
 @Test public void shortNameIsNotTruncated(){assertEquals("Luca",SaintText.abbreviated("San Luca",7));}
 @Test public void longNameHasExactlyTwoDots(){assertEquals("Franc..",SaintText.abbreviated("San Francesco d’Assisi",7));}
 @Test public void apostrophePrefixIsHandled(){assertEquals("Agata",SaintText.abbreviated("Sant’Agata",7));}
 @Test public void searchIgnoresAccentsAndCase(){assertTrue(SaintText.searchable("San André").contains(SaintText.searchable("andre")));}
 @Test public void fullMultiwordNameRemainsSearchable(){assertTrue(SaintText.searchable("San Giovanni Battista").contains("battista"));}
}
