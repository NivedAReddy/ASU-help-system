package simpleDatabase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import Encryption.EncryptionHelper;
import org.junit.jupiter.api.Test;



public class TestCases {
	@Test
	public void encryption() {
		//Test G, testing encryption
		EncryptionHelper encryptionHelper = new EncryptionHelper();
		byte[] encryptedBytes;
		try {
			encryptedBytes = encryptionHelper.encrypt("the quick brown fox jumps over the lazy dog".getBytes());
			String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
	        assertNotEquals("the quick brown fox jumps over the lazy dog", encryptedText);
		} catch (Exception e) {
			assertEquals(1, -1);
		}
	}
	
	@Test
	public void articleGroups() {
		//Test C, testing the creation of of an article group
		ArticleGroup grp = new ArticleGroup("group1", false);
		assertEquals("group1", grp.getGroupName());
		
		//Test D, testing the edit of article group
		grp.addArticle("1");
		Set<String> articleIds = new HashSet<>();
		articleIds.add("1");
		assertEquals(articleIds, grp.getArticleIds());
	}
	
	@Test
	public void helpArticles() {
		//Test A, testing the creation of of help article
		HelpArticle art = new HelpArticle("1", "example title", "potential authors", "very abstract", "Beginner", "some body");
		assertEquals("1", art.getId());
		assertEquals("example title", art.getTitle());
		assertEquals("potential authors", art.getAuthors());
		assertEquals("very abstract", art.getAbstractText());
		assertEquals("Beginner", art.getLevel());
		assertEquals("some body", art.getBody());
		
		//Test B, testing the edit of help article
		art.setBody("brand new body");
		assertEquals("brand new body", art.getBody());
	}
	
	@Test
	public void passwordGen() {
		String tempPass1 = generateTempPassword();
		String tempPass2 = generateTempPassword();
		assertNotEquals(tempPass1, "");
		assertNotEquals(tempPass2, "");
		assertNotEquals(tempPass1, tempPass2);
	}
	
	@Test
	public void inviteGen() {
		String inviteCode1 = generateInviteCode();
		String inviteCode2 = generateInviteCode();
		assertNotEquals(inviteCode1, "");
		assertNotEquals(inviteCode2, "");
		assertNotEquals(inviteCode1, inviteCode2);
	}
	
	@Test
	public void validatePass() {
		boolean isValid = validatePassword("Passwo0!");
		assertEquals(true, isValid);
		isValid = validatePassword("Passwor!");
		assertEquals(false, isValid);
		isValid = validatePassword("Passwor0");
		assertEquals(false, isValid);
		isValid = validatePassword("passwo0!");
		assertEquals(false, isValid);
		isValid = validatePassword("Passw0!");
		assertEquals(false, isValid);
	}
	
	public static String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }
	
	public static String generateTempPassword() {
        Random rnd = new Random();
        int tempPass = 100000 + rnd.nextInt(900000);
        return String.valueOf(tempPass);
    }
	
	public static boolean validatePassword(String pwd) {
        String pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$";
        return Pattern.matches(pattern, pwd);
    }
}