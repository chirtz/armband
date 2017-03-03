package de.chirtz.armband.filter.filter_properties;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.base.Joiner;

import de.chirtz.armband.R;
import de.chirtz.armband.filter.ModifyFilterDialogFragment;

public class PeopleProperty extends AbstractConditionProperty implements ModifyFilterDialogFragment.ActivityResultListener {
    private Fragment fragment;
    private Button peopleButton;
    private final static int REQUEST_PEOPLE_CHOOSER = 113;

    public PeopleProperty(Context context, ViewGroup rootView, Bundle values, Fragment frag) {
        super(context, rootView, values);
        this.fragment = frag;
    }

    public PeopleProperty(Bundle data) {
        super(data);
    }

    @Override
    public String getStateDescription() {
        //return "people " + SBNotification.getPriorityString(getValue()).toLowerCase();
        return null;
    }

    @Override
    public void initialize(LayoutInflater inflater) {
        ((ModifyFilterDialogFragment)fragment).addActivityResultListener(this);
        View v =  inflater.inflate(R.layout.filter_property_people, rootView);
        peopleButton = (Button) v.findViewById(R.id.peopleButton);
        peopleButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
                                                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                                                fragment.startActivityForResult(pickContactIntent, REQUEST_PEOPLE_CHOOSER);
                                            }
                                        }
        );


        if (values.containsKey(FILTER_CONDITION_PEOPLE)) {
            String p = values.getString(FILTER_CONDITION_PEOPLE);
            if (p != null) {
                String[] people = p.split(";;");
                if (people.length == 2) {
                    peopleButton.setText(people[1]);
                    return;
                }
            }
        }
        peopleButton.setText(fragment.getString(R.string.select_person));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PEOPLE_CHOOSER)
            if (resultCode == Activity.RESULT_OK) {
                Uri pickedPhoneNumber = data.getData();
                Cursor cursor = fragment.getActivity().getContentResolver().query(pickedPhoneNumber, null, null, null, null);
                assert cursor != null;
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String key = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    peopleButton.setText(name);
                    setValue(new String[]{key, name});
                }
                cursor.close();
                //values.putAll(data.getExtras());
                //selectAppButton.setText(AppChooserDialogFragment.getAppNameForPackage(context, values.getString(FILTER_CONDITION_PACKAGE)));
            } else {
                peopleButton.setText(fragment.getString(R.string.select_person));
                setValue(null);
            }
    }


    public String getKey() {
        if (!values.containsKey(FILTER_CONDITION_PEOPLE)) return null;
        String people = values.getString(FILTER_CONDITION_PEOPLE);
        if (people == null) return null;
        return people.split(";;")[0];
    }

    private void setValue(String[] val) {
        if (val == null) {
            values.remove(FILTER_CONDITION_PEOPLE);
        } else
            values.putString(FILTER_CONDITION_PEOPLE, Joiner.on(";;").join(val));
    }

    public void setPerson(String[] people, Context con) {
            if (people == null) return;
            Cursor c = con.getContentResolver().query(Uri.parse(people[0]), null, null, null, null);
            while(c.moveToNext()) {
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String key = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                setValue(new String[]{key, name});
            }
            c.close();
    }

    public static void serialize(Bundle source, ContentValues target) {
        target.put(FILTER_CONDITION_PEOPLE, source.getString(FILTER_CONDITION_PEOPLE));
    }

    public static void deserialize(Cursor source, Bundle target) {
        target.putString(FILTER_CONDITION_PEOPLE, source.getString(source.getColumnIndex(FILTER_CONDITION_PEOPLE)));
    }

}
