package de.chirtz.armband.filter;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.TouchViewDraggableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;

import java.util.List;

import de.chirtz.armband.MainActivity;
import de.chirtz.armband.R;

public class FilterFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String TAG = "FilterFragment";
    private static final int REQUEST_ADD_FILTER = 321;
    private static final int REQUEST_MODIFY_FILTER = 11;
    private DynamicListView mListView;
    private FilterListAdapter mAdapter;
    private FilterDatabase filterDatabase;
    private List<Filter> filters;
    private View rootView;
    private static final int UNDO_TIMEOUT = 4000;


    public static FilterFragment newInstance(Context context) {
        FilterFragment fragment = new FilterFragment();
        fragment.filterDatabase = new FilterDatabase(context);
        return fragment;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_filter_list, container, false);
        rootView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        mListView = (DynamicListView) rootView.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.enableDragAndDrop();
        mListView.setDraggableManager(new TouchViewDraggableManager(R.id.handle_view));
        mListView.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int i, int i1) {
                changeFilterPosition(i, i1);
            }
        });

        if (filterDatabase == null)
            filterDatabase = new FilterDatabase(getContext());

        View v = rootView.findViewById(R.id.floating_button);
        if (v != null) {
            FloatingActionButton addButton = (FloatingActionButton) v;
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showModifyFilterDialog(-1);
                }
            });
            addButton.setVisibility(View.VISIBLE);
        }
        loadFilters(false);

        return rootView;
    }

    private class LoadFiltersTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPostExecute(Void result) {
            //mListView.setAdapter(mAdapter);
            TimedUndoAdapter swipeUndoAdapter = new TimedUndoAdapter(mAdapter, FilterFragment.this.getActivity(),
                    new OnDismissCallback() {
                        @Override
                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                removeFilter(position);
                                //mAdapter.remove(position);
                            }
                        }
                    }
            );
            swipeUndoAdapter.setTimeoutMs(UNDO_TIMEOUT);
            swipeUndoAdapter.setAbsListView(mListView);
            mListView.setAdapter(swipeUndoAdapter);
            mListView.enableSimpleSwipeUndo();
            rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (filterDatabase != null) {
                filters = filterDatabase.getEntries();
                if (getActivity() != null) {
                    mAdapter = new FilterListAdapter((AppCompatActivity) getActivity(), filters);
                    //filterDatabase.loadSystemFilters(false);
                }

            }
            return null;
        }

    }

    private void showModifyFilterDialog(int position) {
        DialogFragment fragment;
        if (position < 0) {
            fragment = ModifyFilterDialogFragment.newInstance(getActivity());
            fragment.setTargetFragment(FilterFragment.this, REQUEST_ADD_FILTER);
        } else {
            fragment = ModifyFilterDialogFragment.newInstance(getActivity(), filterDatabase.getEntry(position), position);
            fragment.setTargetFragment(this, REQUEST_MODIFY_FILTER);
        }
        getFragmentManager().beginTransaction().show(fragment).commit();
        fragment.show(getFragmentManager(), ModifyFilterDialogFragment.TAG);
        //getFragmentManager().beginTransaction().addToBackStack(ModifyFilterDialogFragment.TAG).replace(R.id.container, fragment, ModifyFilterDialogFragment.TAG).commit();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showModifyFilterDialog(position);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(mListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.filter_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete_filter:
                removeFilter(info.position);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    private void updateFilter(Filter f) {
        filterDatabase.updateEntry(f);
        filters.get(f.getPosition()).applyValues(f);
        mAdapter.notifyDataSetChanged();
        sendFiltersUpdatedBroadcast();
    }

    private void addFilter(Filter f) {
        filterDatabase.addEntry(f);
        mAdapter.add(f);
        sendFiltersUpdatedBroadcast();
    }

    private void removeFilter(int position) {
        filterDatabase.deleteEntry(position);
        mAdapter.remove(position);
        sendFiltersUpdatedBroadcast();
    }

    private void changeFilterPosition(int oldPos, int newPos) {
        filterDatabase.changeEntryPosition(oldPos, newPos);
        //sendFiltersUpdatedBroadcast();
        loadFilters(true);
    }

    private void loadFilters(boolean broadcast) {
        new LoadFiltersTask().execute();
        if (broadcast)
            sendFiltersUpdatedBroadcast();
    }

    private void sendFiltersUpdatedBroadcast() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null)
            ((MainActivity) getActivity()).sendFiltersUpdatedBroadcast();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MODIFY_FILTER:
                if (resultCode == Activity.RESULT_OK) {
                        updateFilter(new Filter(data));
                    } else
                        Log.d(TAG, "Filter saving failed");
                break;
            case REQUEST_ADD_FILTER:
                if (resultCode == Activity.RESULT_OK) {
                    addFilter(new Filter(data));
                }
                break;
        }
    }





}
