package nl.mout.spellchecker.bloom.hash;

public interface HashFunction {
    byte[] hash(byte[] key);
}
