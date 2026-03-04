package com.gestion.analyzers;

import com.gestion.entities.Blog;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BlogAnalyzerService
 * ───────────────────
 * Pure internal algorithm implementing the content-quality analysis logic
 * described as POST /api/blogs/{id}/analyze.
 *
 * Rules:
 *   - No external or paid APIs
 *   - No database writes — reads only via the Blog entity passed in
 *   - Stateless and thread-safe
 *
 * Scores:
 *   READABILITY (0-100)  based on sentence length, word count, long-sentence ratio
 *   SEO         (0-100)  based on title length, keyword-in-title, content length,
 *                        keyword density, keyword variety
 *   KEYWORDS             top-5 by frequency, stop words excluded
 *   SUGGESTIONS          dynamic, always at least 2, only shown when metric is weak
 */
public class BlogAnalyzerService {

    // ── Stop words (French + English) ─────────────────────────────────────────
    private static final Set<String> STOP = new HashSet<>(Arrays.asList(
        // French
        "le","la","les","un","une","des","du","de","d","l","au","aux",
        "et","ou","mais","donc","or","ni","car","que","qui","quoi","dont",
        "ce","se","sa","son","ses","ma","mon","mes","ta","ton","tes",
        "leur","leurs","nous","vous","ils","elles","je","tu","il","elle",
        "en","dans","sur","sous","par","pour","avec","sans","entre","vers",
        "tout","tous","toute","toutes","plus","moins","tres","bien","aussi",
        "est","sont","etre","avoir","cette","cet","ces","celui","celle",
        "ceux","celles","ne","pas","rien","jamais","toujours","deja",
        "encore","meme","si","comme","quand","alors","apres","avant","y",
        // English
        "the","a","an","of","to","in","is","it","and","or","for","on",
        "at","by","from","with","as","be","was","are","were","been",
        "has","have","had","do","does","did","will","would","could",
        "should","may","might","this","that","these","those","its",
        "not","no","so","if","but","he","she","we","they","you","i",
        "my","your","his","her","our","their","me","him","us","them",
        "what","which","who","how","when","where","than","into","about"
    ));

    // =========================================================================
    //  Public entry point — analyze(blog)
    // =========================================================================

    /**
     * Analyse a blog article.
     * The Blog object is NEVER modified; no database write occurs.
     *
     * @param blog the article object fetched from the database (must not be null)
     * @return fully populated BlogAnalysisResult
     */
    public BlogAnalysisResult analyze(Blog blog) {
        if (blog == null) throw new IllegalArgumentException("blog must not be null");

        String titre   = blog.getTitre()   != null ? blog.getTitre().trim()   : "";
        String contenu = blog.getContenu() != null ? blog.getContenu().trim() : "";
        String fullText = titre + " " + contenu;

        // ── 1. Text metrics ───────────────────────────────────────────────────
        int wordCount     = countWords(contenu);
        int sentenceCount = countSentences(contenu);
        int charCount     = contenu.length();
        double avgWPS     = sentenceCount > 0 ? (double) wordCount / sentenceCount : wordCount;
        String lengthCat  = classifyLength(wordCount);

        // ── 2. Keywords ───────────────────────────────────────────────────────
        Map<String, Integer> freq = buildFrequencyMap(fullText);
        List<String> topKeywords  = extractTop(freq, 5);
        String topKw              = topKeywords.isEmpty() ? "" : topKeywords.get(0);
        double kwDensity = (wordCount > 0 && !topKw.isEmpty())
                ? (double) freq.getOrDefault(topKw, 0) / wordCount * 100.0
                : 0.0;

        // ── 3. Scores ─────────────────────────────────────────────────────────
        int readScore = readabilityScore(wordCount, sentenceCount, avgWPS, contenu);
        int seoScore  = seoScore(titre, wordCount, topKw, kwDensity, topKeywords.size());

        // ── 4. Suggestions ────────────────────────────────────────────────────
        List<String> suggestions = suggestions(
                titre, wordCount, avgWPS, kwDensity, topKw, readScore, seoScore, contenu);

        // ── 5. Round doubles ──────────────────────────────────────────────────
        double avgWPSR  = Math.round(avgWPS    * 10.0) / 10.0;
        double kwDenR   = Math.round(kwDensity * 10.0) / 10.0;

        System.out.println("🔬 Article #" + blog.getIdArticle()
                + "  lisibilite=" + readScore + "  seo=" + seoScore + "  mots=" + wordCount);

        return new BlogAnalysisResult(
                blog.getIdArticle(),
                titre,
                readScore,
                seoScore,
                wordCount,
                sentenceCount,
                charCount,
                avgWPSR,
                lengthCat,
                topKeywords,
                kwDenR,
                suggestions
        );
    }

    // =========================================================================
    //  Readability  (0-100)
    // =========================================================================
    private int readabilityScore(int words, int sentences, double avgWPS, String text) {
        int score = 100;

        if (words < 30)  score -= 20;
        if (words < 50)  score -= 10;

        if      (avgWPS > 25) score -= 30;
        else if (avgWPS > 18) score -= 15;
        else if (avgWPS >= 10 && avgWPS <= 15) score += 5;

        double lr = longSentenceRatio(text);
        if      (lr > 0.40) score -= 15;
        else if (lr > 0.25) score -=  7;
        if (sentences >= 5 && lr > 0.05 && lr < 0.30) score += 5;

        return clamp(score);
    }

    private double longSentenceRatio(String text) {
        if (text == null || text.isBlank()) return 0.0;
        String[] parts = text.split("[.!?]+");
        if (parts.length == 0) return 0.0;
        long n = Arrays.stream(parts).filter(p -> countWords(p) > 25).count();
        return (double) n / parts.length;
    }

    // =========================================================================
    //  SEO score  (0-100)
    // =========================================================================
    private int seoScore(String titre, int words, String topKw,
                         double kwDensity, int distinctKws) {
        int score = 0;
        int tlen  = titre.length();

        if      (tlen >= 40 && tlen <= 65) score += 20;
        else if (tlen >= 20)               score += 10;
        else if (tlen >= 10)               score +=  5;

        if (!topKw.isEmpty() && titre.toLowerCase().contains(topKw.toLowerCase()))
            score += 15;

        if      (words >= 300) score += 20;
        else if (words >= 150) score += 10;
        else if (words >=  80) score +=  5;

        if      (kwDensity >= 1.0 && kwDensity <= 3.0) score += 15;
        else if (kwDensity > 0.5)                       score +=  7;

        if      (distinctKws >= 5) score += 10;
        else if (distinctKws >= 3) score +=  5;

        return clamp(score);
    }

    // =========================================================================
    //  Keyword extraction
    // =========================================================================
    private Map<String, Integer> buildFrequencyMap(String text) {
        if (text == null || text.isBlank()) return Collections.emptyMap();
        Map<String, Integer> freq = new HashMap<>();
        // Normalise: lowercase, keep letters + hyphen/apostrophe
        String[] tokens = text.toLowerCase()
                .replaceAll("[^a-z\\u00e0-\\u00ff'\\-\\s]", " ")
                .split("\\s+");
        for (String tok : tokens) {
            tok = tok.replaceAll("^['-]+|['-]+$", "").trim();
            if (tok.length() < 3)       continue;
            if (STOP.contains(tok))     continue;
            if (tok.matches("\\d+"))    continue;
            freq.merge(tok, 1, Integer::sum);
        }
        return freq;
    }

    private List<String> extractTop(Map<String, Integer> freq, int n) {
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // =========================================================================
    //  Suggestions — dynamic, always >= 2
    // =========================================================================
    private List<String> suggestions(String titre, int words, double avgWPS,
            double kwDensity, String topKw, int readScore, int seoScore, String contenu) {

        List<String> s = new ArrayList<>();

        if (words < 150)
            s.add("Augmentez le contenu : " + words + " mots actuellement. Visez 300+ pour le SEO.");
        else if (words < 300)
            s.add("Développez jusqu'à 300+ mots (" + words + " actuellement).");

        if (avgWPS > 25)
            s.add("Phrases trop longues (moy. " + Math.round(avgWPS)
                    + " mots). Découpez-les pour améliorer la lisibilité.");
        else if (avgWPS > 18)
            s.add("Raccourcissez certaines phrases (moy. " + Math.round(avgWPS)
                    + " mots ; idéal : 15–18).");

        int tlen = titre.length();
        if (tlen < 40)
            s.add("Titre trop court (" + tlen + " car.). Visez 40–65 caractères pour le SEO.");
        else if (tlen > 65)
            s.add("Titre trop long (" + tlen + " car.). Réduisez à 65 max.");

        if (!topKw.isEmpty() && !titre.toLowerCase().contains(topKw.toLowerCase()))
            s.add("Intégrez le mot-clé « " + topKw + " » dans le titre.");

        if (kwDensity > 5.0)
            s.add("Densité de « " + topKw + " » trop élevée ("
                    + Math.round(kwDensity) + "%). Réduisez les répétitions.");
        else if (kwDensity < 0.5 && words >= 100)
            s.add("Densité de mots-clés très faible. Intégrez naturellement les termes importants.");

        boolean hasStructure = contenu.contains("##") || contenu.contains("**")
                || contenu.contains("<h") || contenu.contains("==");
        if (!hasStructure && words > 200)
            s.add("Ajoutez des sous-titres pour structurer le contenu et améliorer le SEO.");

        // Guarantee at least 2
        if (s.isEmpty())
            s.add("Ajoutez des liens internes vers d'autres articles pour renforcer la navigation.");
        if (s.size() < 2)
            s.add("Ajoutez une image illustrative avec un texte alternatif (alt text) descriptif.");

        return s;
    }

    // =========================================================================
    //  Text helpers
    // =========================================================================
    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    private int countSentences(String text) {
        if (text == null || text.isBlank()) return 0;
        long n = Arrays.stream(text.split("[.!?]+"))
                .filter(p -> !p.isBlank()).count();
        return (int) Math.max(1, n);
    }

    private String classifyLength(int words) {
        if (words < 100)  return "Court";
        if (words <= 400) return "Moyen";
        return "Long";
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}
