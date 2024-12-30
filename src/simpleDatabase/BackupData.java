package simpleDatabase;

import java.io.Serializable;
import java.util.Map;

public class BackupData implements Serializable {
    private static final long serialVersionUID = 1L;

    public Map<String, HelpArticle> articleMap;
    public Map<String, ArticleGroup> groupMap;
}
