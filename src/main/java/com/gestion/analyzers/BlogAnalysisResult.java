package com.gestion.analyzers;

import java.util.List;

/**
 * BlogAnalysisResult
 * ──────────────────
 * Immutable result object returned by BlogAnalyzerService.
 * Every field is assigned exactly once in the constructor.
 * This class never accesses the database and has no JavaFX dependency.
 *
 * Mirrors the JSON shape from the specification:
 *   readability_score, seo_score, word_count, top_keywords, suggestions
 */
public class BlogAnalysisResult {

    private final int    articleId;
    private final String articleTitre;

    private final int    readabilityScore;
    private final int    seoScore;
    private final int    wordCount;
    private final int    sentenceCount;
    private final int    characterCount;
    private final double avgWordsPerSentence;
    private final String contentLengthCategory;

    private final List<String> topKeywords;
    private final double       keywordDensity;      // ← field declared AND assigned below
    private final List<String> suggestions;

    // =========================================================================
    //  Constructor — all 12 fields assigned, no omissions
    // =========================================================================
    public BlogAnalysisResult(
            int          articleId,
            String       articleTitre,
            int          readabilityScore,
            int          seoScore,
            int          wordCount,
            int          sentenceCount,
            int          characterCount,
            double       avgWordsPerSentence,
            String       contentLengthCategory,
            List<String> topKeywords,
            double       keywordDensity,
            List<String> suggestions) {

        this.articleId             = articleId;
        this.articleTitre          = articleTitre;
        this.readabilityScore      = readabilityScore;
        this.seoScore              = seoScore;
        this.wordCount             = wordCount;
        this.sentenceCount         = sentenceCount;
        this.characterCount        = characterCount;
        this.avgWordsPerSentence   = avgWordsPerSentence;
        this.contentLengthCategory = contentLengthCategory;
        this.keywordDensity        = keywordDensity;           // always assigned here
        this.topKeywords  = (topKeywords  != null) ? List.copyOf(topKeywords)  : List.of();
        this.suggestions  = (suggestions  != null) ? List.copyOf(suggestions)  : List.of();
    }

    // =========================================================================
    //  Getters
    // =========================================================================
    public int          getArticleId()             { return articleId; }
    public String       getArticleTitre()          { return articleTitre; }
    public int          getReadabilityScore()      { return readabilityScore; }
    public int          getSeoScore()              { return seoScore; }
    public int          getWordCount()             { return wordCount; }
    public int          getSentenceCount()         { return sentenceCount; }
    public int          getCharacterCount()        { return characterCount; }
    public double       getAvgWordsPerSentence()   { return avgWordsPerSentence; }
    public String       getContentLengthCategory() { return contentLengthCategory; }
    public List<String> getTopKeywords()           { return topKeywords; }
    public double       getKeywordDensity()        { return keywordDensity; }
    public List<String> getSuggestions()           { return suggestions; }
}
