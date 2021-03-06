package it.cf.bloodhoud.client.android.model;

import it.cf.bloodhoud.client.android.Utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactManager
    {
        static private final Logger   LOG              = LoggerFactory.getLogger(ContactManager.class);
        private Context               context          = null;
        private static ContactManager contactManager   = null;
        private Map<String, String>   contactNameCache = new Hashtable<String, String>();

        private ContactManager(final Context context) throws Exception
            {
                if (context == null)
                    {
                        LOG.error("context == null");
                        throw new Exception("context == null");
                    }
                this.context = context;
            }

        static public ContactManager getInstance(final Context context) throws Exception
            {
                if (contactManager == null)
                    {
                        contactManager = new ContactManager(context);
                    }
                return contactManager;
            }

        public String getContactNameFromNumber(final String phoneNumber) throws Exception
            {
                String phoneNumberWithoutInternationalPrefix = Utils.getPhoneNumberWithoutInternationalPrefix(phoneNumber);
                LOG.debug("Phone number = {}, without prefix = {}", phoneNumber, phoneNumberWithoutInternationalPrefix);

                String contactName = getContactNameFromCache(phoneNumberWithoutInternationalPrefix);
                if (StringUtils.isBlank(contactName))
                    {
                        contactName = getContactNameFromContacts(phoneNumberWithoutInternationalPrefix);
                    }
                LOG.debug("ContactName = {}", contactName);
                return contactName;
            }

        private String getContactNameFromCache(String phoneNumberWithoutInternationalPrefix)
            {
                String contactName = "";
                if (contactNameCache.containsKey(phoneNumberWithoutInternationalPrefix))
                    {
                        contactName = contactNameCache.get(phoneNumberWithoutInternationalPrefix);
                        LOG.debug("ContactName = {}. Preso dalla cache", contactName);
                    }
                return contactName;
            }

        private String getContactNameFromContacts(final String phoneNumberWithoutInternationalPrefix) throws Exception
            {
                String contactName = "";
                // for read ALL (phone + sim) contact is necessary uses-permission="android.permission.READ_CONTACTS"
                // Cursor contactCursor = getContactsCursor(context);
                Cursor contactCursor = getCursor4ContactsWithPhoneNumber(context);
                LOG.trace("Num conctact with phone number = {}", contactCursor.getCount());
                while (contactCursor.moveToNext())
                    {
                        String contactId = getContactId(contactCursor);
                        LOG.trace("contactId = {}, ContactName = {}", contactId, getContactName(contactCursor));
                        List<String> contactPhoneNumbers = getPhoneNumbersByContactId(context, contactId);
                        if (listOfPhoneNumbersContainsPhoneNumber(contactPhoneNumbers, phoneNumberWithoutInternationalPrefix))
                            {
                                contactName = getContactName(contactCursor);
                            }
                    }
                contactCursor.close();
                if (contactName.length() == 0)
                    {
                        contactName = "UNKNOW";
                    }
                contactNameCache.put(phoneNumberWithoutInternationalPrefix, contactName);

                LOG.debug("ContactName = {}. Messo in cache", contactName);
                return contactName;

            }

        private boolean listOfPhoneNumbersContainsPhoneNumber(final List<String> contactPhoneNumbers, final String phoneNumberWithoutInternationalPrefix)
            {
                LOG.debug("Phone number list = {}", contactPhoneNumbers);

                boolean isPresentIntoList = false;
                String phoneNumber = Utils.getPhoneNumberWithInternationalPrefix(phoneNumberWithoutInternationalPrefix);
                isPresentIntoList = contactPhoneNumbers.contains(phoneNumber);
                LOG.debug("Phone number with international prefix {} is in phone list: {}", phoneNumber, isPresentIntoList);

                phoneNumber = Utils.getPhoneNumberWithoutInternationalPrefix(phoneNumber);
                isPresentIntoList = isPresentIntoList || contactPhoneNumbers.contains(phoneNumber);
                LOG.debug("Phone number without international prefix {} is in phone list: {}", phoneNumber, isPresentIntoList);
                return isPresentIntoList;
            }

        private Cursor getCursor4ContactsWithPhoneNumber(Context context) throws Exception
            {
                if (context == null)
                    {
                        LOG.error("Il context non deve essere null");
                        throw new Exception("Il context non deve essere null");
                    }

                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER };
                // String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
                String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
                // String[] selectionArgs = null;

                String[] selectionArgs = new String[] { "1" };
                String sortOrder = null; // ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

                Cursor cursor;
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

                /*
                 * Valid from Android 3.0.x (HONEYCOMB) Api Level = 11 CursorLoader cl = new CursorLoader(context, uri, projection, selection, selectionArgs,
                 * sortOrder); cursor=cl.loadInBackground();
                 */
                return cursor;
            }

        private List<String> getPhoneNumbersByContactId(Context context, String contactId) throws Exception
            {
                if (context == null)
                    {
                        LOG.error("Il context non deve essere null");
                        throw new Exception("Il context non deve essere null");
                    }
                if ((contactId == null) || (contactId.length() == 0))
                    {
                        LOG.error("Il contactId non deve essere null");
                        throw new Exception("Il context non deve essere null o vuoto");
                    }

                List<String> phoneNumbers = new ArrayList<String>();

                Cursor phonesCursor = getCursor4PhoneNumberCursorForContactId(context, contactId);
                while (phonesCursor.moveToNext())
                    {
                        String phoneNumber = getPhoneNumber(phonesCursor);
                        phoneNumbers.add(phoneNumber);

                        LOG.trace("PhoneNumber = {}", phoneNumber);
                    }
                phonesCursor.close();
                return phoneNumbers;

            }

        private Cursor getCursor4PhoneNumberCursorForContactId(Context context, String contactId) throws Exception
            {
                if (context == null)
                    {
                        LOG.error("Il context non deve essere null");
                        throw new Exception("Il context non deve essere null");
                    }
                if ((contactId == null) || (contactId.length() == 0))
                    {
                        LOG.error("Il contactId non deve essere null");
                        throw new Exception("Il context non deve essere null o vuoto");
                    }

                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
                // String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?";
                // String[] selectionArgs = null;
                String[] selectionArgs = new String[] { contactId };
                String sortOrder = null;

                Cursor cursor;
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

                /*
                 * Valid from Android 3.0.x (HONEYCOMB) Api Level = 11 CursorLoader cl = new CursorLoader(context, uri, projection, selection, selectionArgs,
                 * sortOrder); cursor=cl.loadInBackground();
                 */
                return cursor;
            }

        private String getContactId(Cursor contactCursor) throws Exception
            {
                return getStringValueFromColumn(ContactsContract.Contacts._ID, contactCursor);
            }

        private String getContactName(Cursor contactCursor) throws Exception
            {
                // return getStringValueFromColumn(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contactCursor);
                return getStringValueFromColumn(ContactsContract.Contacts.DISPLAY_NAME, contactCursor);
            }

        private String getPhoneNumber(Cursor contactCursor) throws Exception
            {
                return getStringValueFromColumn(ContactsContract.CommonDataKinds.Phone.NUMBER, contactCursor);
            }

        private String getStringValueFromColumn(String columnName, Cursor contactCursor) throws Exception
            {
                if ((columnName == null) || (columnName.length() == 0))
                    {
                        LOG.error("Il nome della colonna non deve essere null o vuoto");
                        throw new Exception("Il nome della colonna non deve essere null o vuoto");
                    }
                if ((contactCursor == null) || (contactCursor.isClosed()))
                    {
                        LOG.error("Il cursore non deve essere null o  chiuso");
                        throw new Exception("Il cursore non deve essere null o  chiuso");
                    }

                String stringValue = "";
                try
                    {
                        int indexColumn = contactCursor.getColumnIndexOrThrow(columnName);
                        LOG.trace("Nome colonna <{}> ha indice {} ", columnName, String.valueOf(indexColumn));

                        stringValue = contactCursor.getString(indexColumn);
                    }
                catch (Exception e)
                    {
                        LOG.error(e.getMessage());
                        stringValue = "";
                    }
                return stringValue;
            }

    }
