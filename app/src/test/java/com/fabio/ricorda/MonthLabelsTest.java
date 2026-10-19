package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.util.Locale;

public class MonthLabelsTest {
 @Test public void italianMonthIsCapitalizedInsideFullDate(){assertEquals("19 Ottobre 2026",MonthLabels.format(LocalDate.of(2026,10,19),Locale.ITALIAN,"d MMMM yyyy"));}
 @Test public void weekdayIsNotChanged(){assertEquals("lunedì 19 Ottobre",MonthLabels.format(LocalDate.of(2026,10,19),Locale.ITALIAN,"EEEE d MMMM"));}
 @Test public void quotedPatternLettersStayLiteral(){assertEquals("MMMM 19 Ottobre",MonthLabels.format(LocalDate.of(2026,10,19),Locale.ITALIAN,"'MMMM' d MMMM"));}
 @Test public void frenchAccentsAndTurkishCasingArePreserved(){assertEquals("Février",MonthLabels.format(LocalDate.of(2032,2,29),Locale.FRENCH,"LLLL"));assertEquals("İlk",MonthLabels.initialUpper("ilk",Locale.forLanguageTag("tr")));}
 @Test public void inflectedAndStandaloneMonthsRemainDistinct(){LocalDate d=LocalDate.of(2030,1,1);Locale ru=Locale.forLanguageTag("ru");assertEquals("Январь",MonthLabels.format(d,ru,"LLLL"));assertEquals("1 Января",MonthLabels.format(d,ru,"d MMMM"));}
 @Test public void numericMonthsAndUncasedScriptsAreNotAltered(){LocalDate d=LocalDate.of(2036,2,29);assertEquals("29/02/2036",MonthLabels.format(d,Locale.ITALIAN,"dd/MM/yyyy"));assertEquals("2月",MonthLabels.format(d,Locale.JAPANESE,"LLLL"));}
 @Test public void allTwelveItalianMonthsStartUppercase(){for(int m=1;m<=12;m++){String s=MonthLabels.format(LocalDate.of(2036,m,1),Locale.ITALIAN,"LLLL");assertTrue(s,Character.isUpperCase(s.codePointAt(0)));}}
}
