package edu.washington.cs.dericp.diffutils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * This class is a container for multiple SingleFilePatches.
 */
public class Patch {
    private List<SingleFilePatch> singleFilePatches;

    public List<LineChange> getChanges() {
        throw new NotImplementedException();
    }

    public List<List<LineChange>> getContiguousChanges() {
        throw new NotImplementedException();
    }
}
