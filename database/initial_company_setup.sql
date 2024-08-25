-- inserting company with (optional) logo and inserting initial admin user for company
DO $$
    DECLARE
        company_name TEXT := 'Company Name';
        company_domain TEXT := 'sample-company';
--         ensure that user which runs postgresql service and user in postgresql has read access to the file
        company_logo_path TEXT := 'C:\Users\Krzysiek\Pictures\zdj_2020.jpg';
        company_logo_id INT;
        company_id INT;
--         data of the initial admin user
        _username TEXT := 'username';
        _password TEXT := 'password';
        _first_name TEXT := 'First Name';
        _last_name TEXT := 'Last Name';
--        admin role corresponds to 0 ordinal, 1 - client, 2 - courier
        _role INT := 1;
        _temporary_password BOOLEAN := false;
        _hashed_password TEXT;
    BEGIN
        IF company_logo_path <> '' THEN
            RAISE NOTICE 'Company logo path: %, script will try to read and save image as a company logo.', company_logo_path;
            INSERT INTO "logistics-management-app".public.company_logo (image) VALUES (pg_read_binary_file(company_logo_path))
            RETURNING id INTO company_logo_id;
        ELSE
            company_logo_id := NULL;
            RAISE NOTICE 'Company logo path is empty so no image will be saved as a company logo.';
        END IF;
        INSERT INTO "logistics-management-app".public.company (name, domain, logo_id) VALUES (company_name, company_domain, company_logo_id)
        RETURNING id INTO company_id;

--         to apply password hashing you must have pgcrypto extension installed, it can be done with command: 'create extension pgcrypto'
        _hashed_password := crypt(_password, gen_salt('bf'));
        INSERT INTO "logistics-management-app".public."user" (username, password, first_name, last_name, company, role, temporary_password)
        VALUES (_username, _hashed_password, _first_name, _last_name, company_id, _role, _temporary_password);
END$$;
