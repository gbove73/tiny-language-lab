package dev.gbove73.tinylanguagelab.model;

import java.util.List;

/**
 * Divide una stringa in punti di codice Unicode.
 *
 * <p>Un tokenizzatore reale raggruppa spesso frammenti di parola. Qui scegliamo i caratteri
 * perché il lettore possa seguire ogni passaggio senza dizionari o librerie nascoste.</p>
 */
public final class CharacterTokenizer {

    public List<String> tokenize(String text) {
        // L'uso di codePoints gestisce correttamente anche simboli fuori dal BMP, come molte emoji.
        return text.codePoints()
                .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                .toList();
    }
}
