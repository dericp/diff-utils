package edu.washington.cs.dericp.diffutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A UnifiedDiff represents a unified diff.
 */
public class UnifiedDiff {
    
    // This field changes depending on what signifies a new diff.
    // In some formats, this could be "diff", and in others, "---".
    public static final String DIFF_SPLIT = "diff";
    private List<Diff> diffs;
    
    
    /**
     * Constructs a UnifiedDiff object from the specified unified diff at
     * the relative filepath relFilePath.
     * @param relFilePath
     */
    public UnifiedDiff(String relFilePath) {
        this(Utils.fileToLines(relFilePath));
    }
    
    /**
     * Constructs a UnifiedDiff with the specified diff file lines.
     * 
     * @param unifiedDiffLines  the lines of the unified diff
     */
    public UnifiedDiff(List<String> unifiedDiffLines) {
        readDiffs(unifiedDiffLines);
    }
    
    /**
     * Constructs a UnifiedDiff that is a copy of unifiedDiff.
     * 
     * @param other is the unified diff to be copied
     */
    public UnifiedDiff(UnifiedDiff unifiedDiff) {
        diffs = new ArrayList<Diff>();
        for (Diff diff : unifiedDiff.diffs) {
            diffs.add(new Diff(diff));
        }
    }
    
    /**
     * Reads in the lines of a unified diff.
     * 
     * @param unifiedDiffLines are the lines of the unified diff
     */
    private void readDiffs(List<String> unifiedDiffLines) {
        diffs = new ArrayList<Diff>();
        if (unifiedDiffLines.isEmpty()) {
            throw new IllegalArgumentException("Diff is empty");
        }
        
        Iterator<String> iter = unifiedDiffLines.iterator();
        
        // TODO this fence-post has potential to be very buggy
        String line = iter.next();
        while (iter.hasNext()) {
            if (line.startsWith(DIFF_SPLIT)) {
                List<String> diffLines = new ArrayList<String>();
                diffLines.add(line);
                
                line = iter.next();
                
                while (!line.startsWith(DIFF_SPLIT) && iter.hasNext()) {
                    diffLines.add(line);
                    line = iter.next();
                }
                
                // if last line of unifiedDiff
                if (!iter.hasNext()) {
                    diffLines.add(line);
                }
                diffs.add(new Diff(diffLines));
            } else {
                line = iter.next();
            }
        }
    }
    
    /**
     * Returns the diffs that compose this unified diff.
     * 
     * @return the diffs of the unified diff
     */
    public List<Diff> getDiffs() {
        return diffs;
    }
    
    /**
     * Removes a diff from the unified diff. Conceptually, this method undoes
     * all the changes in an entire file from the unified diff.
     * 
     * @param diffNumber is the zero-based index of the diff to be removed
     */
    public void removeDiff(int diffNumber) {
        // Currently implemented as setting the diff in diffs to null since
        // it is beneficial to know how many diffs the unified diff started
        // with.
        diffs.set(diffNumber, null);
    }
    
    /**
     * Removes a hunk from the unified diff. Conceptually, this
     * method undoes all the changes in an entire hunk.
     * 
     * @param diffNumber is the zero-based index of the specified diff
     * @param hunkNumber is the zero-based index of the hunk to be removed
     */
    public void removeHunk(int diffNumber, int hunkNumber) {
        List<Hunk> hunks = diffs.get(diffNumber).getHunks();
        Hunk removedHunk = hunks.get(hunkNumber);
        int offset = removedHunk.getOriginalHunkSize() - removedHunk.getRevisedHunkSize();
        for (int i = hunkNumber + 1; i < diffs.get(diffNumber).getHunks().size(); ++i) {
           hunks.get(i).modifyRevisedLineNumber(offset);
        }
        hunks.set(hunkNumber, null);
    }
    
    /**
     * Removes a change in the unified diff. Conceptually, this undoes a single
     * change in a hunk.
     * 
     * @param diffNumber is the zero-based index of the diff that the change
     *                   is contained in
     * @param hunkNumber is the zero-based index of the hunk that the change
     *                   is contained in
     * @param lineNumber is the zero-based index of the line number 
     */
    public void removeChangeFromHunk(int diffNumber, int hunkNumber, int lineNumber) {
        List<Hunk> hunks = diffs.get(diffNumber).getHunks();
        Hunk modifiedHunk = hunks.get(hunkNumber);
        int result = modifiedHunk.removeLine(lineNumber);
        if (result != 0) {
            for (int i = hunkNumber + 1; i < hunks.size(); i++) {
                Hunk currentHunk = hunks.get(i);
                if (currentHunk != null) {
                    if (result == 1) {
                        hunks.get(i).modifyRevisedLineNumber(-1);
                    }
                    if (result == -1) {
                        hunks.get(i).modifyRevisedLineNumber(1);
                    }
                }
            }
        }
    }
    
    /**
     * Exports the unified diff to a file.
     * 
     * @param filePath  the file path where the unified diff will be exported
     */
    public void exportUnifiedDiff(String filePath) {
        List<String> export = new ArrayList<String>();
        for (Diff diff : diffs) {
            export.addAll(diff.diffToLines());
        }
        Utils.linesToFile(export, filePath);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UnifiedDiff)) return false;
        
        UnifiedDiff other = (UnifiedDiff) obj;
        
        return (diffs.equals(other.diffs));
    }
    
    @Override
    public int hashCode() {
        return diffs.hashCode();
    }
}
