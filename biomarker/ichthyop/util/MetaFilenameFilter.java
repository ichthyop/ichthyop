package ichthyop.util;

import java.util.regex.Pattern;
import java.io.File;

////////////////////////////////////////////////////
public class MetaFilenameFilter
    implements java.io.FilenameFilter {

  /** La pattern regexp correspondant aux meta-caractères */
  private final Pattern pattern;

  /**
   * Constructeur.
   * @param term La chaine représentant le masque de nom de fichier.
   */
  public MetaFilenameFilter(String fileMask) {
    // Ajout de \Q \E autour des sous-chaines de fileMask
    // qui ne sont pas des meta-caractères :
    String regexpPattern = fileMask.replaceAll("[^\\*\\?]+", "\\\\Q$0\\\\E");
    // On remplace toutes les occurrences de '*' afin de les interpréter :
    regexpPattern = regexpPattern.replaceAll("\\*", ".*");
    // On remplace toutes les occurrences de '?' afin de les interpréter :
    regexpPattern = regexpPattern.replaceAll("\\?", ".");
    // On crée le pattern :
    this.pattern = Pattern.compile(regexpPattern);

  }

  public boolean accept(File dir, String name) {
    return this.pattern.matcher(name).matches();
  }

}
