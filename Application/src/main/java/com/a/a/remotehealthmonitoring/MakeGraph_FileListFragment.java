package com.a.a.remotehealthmonitoring;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.widget.ListView;

import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.io.File;
import java.util.List;

public class MakeGraph_FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<File>> {
    public interface Callbacks {
        /**
         * Called when a file is selected from the list.
         *
         * @param file The file selected
         */
        public void onFileSelected(File file);
    }

    private static final int LOADER_ID = 0;

    private MakeGraph_FileListAdapter mAdapter;
    private String mPath;

    private Callbacks mListener;

    /**
     * Create a new instance with the given file path.
     *
     * @param path The absolute path of the file (directory) to display.
     * @return A new Fragment with the given file path.
     */
    public static MakeGraph_FileListFragment newInstance(String path) {
        MakeGraph_FileListFragment fragment = new MakeGraph_FileListFragment();
        Bundle args = new Bundle();
        args.putString(MakeGraph_FileChooserActivity.PATH, path);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FileListFragment.Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MakeGraph_FileListAdapter(getActivity());
        mPath = getArguments() != null ? getArguments().getString(
                MakeGraph_FileChooserActivity.PATH) : Environment
                .getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(getString(R.string.empty_directory));
        setListAdapter(mAdapter);
        setListShown(false);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MakeGraph_FileListAdapter adapter = (MakeGraph_FileListAdapter) l.getAdapter();
        if (adapter != null) {
            File file = (File) adapter.getItem(position);
            mPath = file.getAbsolutePath();
            mListener.onFileSelected(file);
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        return new MakeGraph_FileLoader(getActivity(), mPath);
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        mAdapter.setListItems(data);

        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {
        mAdapter.clear();
    }
}